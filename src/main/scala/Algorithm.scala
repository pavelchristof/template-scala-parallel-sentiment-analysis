package org.template.sentiment

import grizzled.slf4j.Logger
import io.prediction.controller._
import org.apache.commons.math3.random.MersenneTwister
import org.apache.spark.SparkContext
import org.deeplearning4j.models.rntn.RNTN
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.text.corpora.treeparser.{TreeParser, TreeVectorizer}
import org.deeplearning4j.text.inputsanitation.InputHomogenization
import org.deeplearning4j.text.sentenceiterator.{CollectionSentenceIterator, SentencePreProcessor}
import org.deeplearning4j.text.tokenization.tokenizerfactory.UimaTokenizerFactory
import org.nd4j.linalg.api.activation.Activations

import scala.collection.JavaConversions._

object PreProcessor extends SentencePreProcessor {
  override def preProcess(s: String): String =
    new InputHomogenization(s).transform()
}

case class AlgorithmParams (
  val windowSize: Int,
  val layerSize: Int
) extends Params

class Algorithm(val params: AlgorithmParams)
  extends P2LAlgorithm[PreparedData, Model, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, data: PreparedData): Model = {
    val sentences = data.unlabeled.collect.toSeq
    val sentenceIterator = new CollectionSentenceIterator(PreProcessor, sentences)
    val tokenizerFactory = new UimaTokenizerFactory()
    val word2vec = new Word2Vec.Builder()
      .windowSize(params.windowSize).layerSize(params.layerSize)
      .iterate(sentenceIterator)
      .tokenizerFactory(tokenizerFactory)
      .build()
    word2vec.fit()

    val rntn = new RNTN.Builder()
      .setActivationFunction(Activations.hardTanh)
      .setFeatureVectors(word2vec)
      .setUseTensors(true)
      .setRng(new MersenneTwister(123))
      .build()

    val labels = data.labels
    val labeled = data.labeled.collect
    val vectorizer = createVectorizer()
    labeled.foreach(tw => {
      val trees = vectorizer.getTreesWithLabels(tw.text, tw.sentiment, labels)
      rntn.fit(trees)
    })

    new Model(word2vec, rntn, labels)
  }

  def predict(model: Model, query: Query): PredictedResult = {
    val trees = createVectorizer().getTreesWithLabels(query.text, model.labels)
    val scores = model.rntn.predict(trees)
    PredictedResult(scores.map(model.labels.get(_)).toArray)
  }

  private def createVectorizer() =
    new TreeVectorizer(new TreeParser())
}

class Model(
  val word2vec: Word2Vec,
  val rntn: RNTN,
  val labels: List[String]
) extends Serializable
