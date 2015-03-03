package org.template.word2vec

import grizzled.slf4j.Logger
import io.prediction.controller._
import org.deeplearning4j.models.embeddings.WeightLookupTable
import org.deeplearning4j.models.word2vec.wordstore.VocabCache
import org.deeplearning4j.spark.models.word2vec.Word2Vec
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.{BlasWrapper, Nd4j}
import org.nd4j.linalg.ops.transforms.Transforms
import scala.collection.JavaConverters._

class Algorithm
  extends P2LAlgorithm[PreparedData, Model, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(data: PreparedData): Model = {
    val w2v = new Word2Vec()
    val r = w2v.train(data.sentences)
    new Model(r.getFirst, r.getSecond)
  }

  def predict(model: Model, query: Query): PredictedResult = {
    val vecA: INDArray = model.weights.vector(query.word)
    val vecAU: INDArray = Transforms.unitVec(vecA)

    val words = model.vocabCache.words.iterator.asScala.filter(_ != query.word)
    val wordsWithScores = words.map(word => {
      val vecB: INDArray = model.weights.vector(word)
      val vecBU: INDArray = Transforms.unitVec(vecB)

      (word, blas.dot(vecAU, vecBU))
    })

    val similar = wordsWithScores
      .toArray
      .sortBy(_._2)(Ordering[Double].reverse)
      .take(query.num)
      .map(_._1)
    PredictedResult(similar)
  }

  private def blas: BlasWrapper[INDArray] =
    Nd4j.getBlasWrapper.asInstanceOf[BlasWrapper[INDArray]]
}

class Model(val vocabCache: VocabCache,
            val weights: WeightLookupTable)
  extends Serializable {}
