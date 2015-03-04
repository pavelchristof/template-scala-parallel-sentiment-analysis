package org.template.sentiment

import grizzled.slf4j.Logger
import io.prediction.controller._
import org.apache.spark.SparkContext
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.text.inputsanitation.InputHomogenization
import org.deeplearning4j.text.sentenceiterator.{CollectionSentenceIterator, SentencePreProcessor}
import org.deeplearning4j.text.tokenization.tokenizerfactory.UimaTokenizerFactory

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
    val sentences = data.sentences.collect.toSeq
    val sentenceIterator = new CollectionSentenceIterator(PreProcessor, sentences)
    val tokenizerFactory = new UimaTokenizerFactory()
    val word2vec = new Word2Vec.Builder()
      .windowSize(params.windowSize).layerSize(params.layerSize)
      .iterate(sentenceIterator)
      .tokenizerFactory(tokenizerFactory)
      .build()

    word2vec.fit()
    new Model(word2vec)
  }

  def predict(model: Model, query: Query): PredictedResult = {
    val words = model.word2vec.wordsNearest(query.word, query.num)
    PredictedResult(words.toSeq.toArray)
  }
}

class Model(
  val word2vec: Word2Vec
) extends Serializable {}
