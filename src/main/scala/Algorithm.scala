package org.template.word2vec

import grizzled.slf4j.Logger
import io.prediction.controller.{P2LAlgorithm, Params}
import org.deeplearning4j.models.embeddings.WeightLookupTable
import org.deeplearning4j.models.word2vec.wordstore.VocabCache
import org.deeplearning4j.spark.models.word2vec.{Word2VecPerformer, Word2Vec}

case class AlgorithmParams(mult: Int) extends Params

class Algorithm(val ap: AlgorithmParams)
  extends P2LAlgorithm[PreparedData, Model, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(data: PreparedData): Model = {
    val sc = data.sentences.sparkContext
    val w2v = new Word2Vec()
    sc.getConf.set(Word2VecPerformer.NEGATIVE, String.valueOf(0))
    val r = w2v.train(data.sentences)
    new Model(r.getFirst, r.getSecond)
  }

  def predict(model: Model, query: Query): PredictedResult = {
    val vec = model.weights.vector(query.word)
    val arr = Array.range(0, vec.columns() - 1).map(vec.getDouble)
    PredictedResult(arr)
  }
}

class Model(val vocabCache: VocabCache,
            val weights: WeightLookupTable)
  extends Serializable {}
