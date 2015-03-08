package org.template.sentiment

import grizzled.slf4j.Logger
import io.prediction.controller._
import org.apache.commons.math3.random.MersenneTwister
import org.apache.spark.SparkContext
import org.deeplearning4j.models.featuredetectors.autoencoder.recursive.Tree
import org.deeplearning4j.models.rntn.RNTN
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.text.corpora.treeparser.{TreeParser, TreeVectorizer}
import org.deeplearning4j.text.inputsanitation.InputHomogenization
import org.deeplearning4j.text.sentenceiterator.{CollectionSentenceIterator, SentencePreProcessor}
import org.deeplearning4j.text.tokenization.tokenizerfactory.UimaTokenizerFactory
import org.nd4j.linalg.api.ndarray.INDArray

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object PreProcessor extends SentencePreProcessor {
  override def preProcess(s: String): String =
    new InputHomogenization(s).transform()
}

object ND4JUtils {
  def toDoubleArray(iNDArray: INDArray): Array[Double] =
    Array.tabulate(iNDArray.length)(iNDArray.getDouble)
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
      .setActivationFunction("tanh")
      .setFeatureVectors(word2vec)
      .setRng(new MersenneTwister())
      .setCombineClassification(false)
      .build()

    val labels = data.labels
    val trees = data.labeled
      .mapPartitions(vectorize(_, labels))
      .filter(_._2.nonEmpty)
      .map { case (tweet, trees) => {
        val root = mergeTrees(trees, labels)
        root.setLabel(tweet.sentiment)
        root
      }}
      .collect()

    val future = rntn.fit(trees.toList)
    Await.ready(future, Duration.Inf)

    rntn.getActorSystem.shutdown()
    new Model(word2vec, rntn, labels)
  }

  def vectorize(texts: Iterator[Tweet], labels: List[String]) = {
    val vectorizer = getVectorizer()
    texts.map(tweet => {
      val prepText = PreProcessor.preProcess(tweet.text)
      val trees = vectorizer.getTreesWithLabels(prepText, labels)
      (tweet, trees)
    })
  }

  def mergeTrees(trees: Seq[Tree], labels: List[String]) =
    trees.reduceRight((a, b) => {
      val parent = new Tree(labels)
      parent.connect(List(a, b))
      parent
    })

  def predict(model: Model, query: Query): PredictedResult = {
    val text = PreProcessor.preProcess(query.text)
    val trees = getVectorizer().getTreesWithLabels(text, model.labels)
    val result = model.rntn.output(List(mergeTrees(trees, model.labels)))
    val scores = ND4JUtils.toDoubleArray(result.head)
    PredictedResult(model.labels.zip(scores).toArray)
  }

  private def getVectorizer() =
    new TreeVectorizer(new TreeParser())
}

class Model(
  val word2vec: Word2Vec,
  val rntn: RNTN,
  val labels: List[String]
) extends Serializable
