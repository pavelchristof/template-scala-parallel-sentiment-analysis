package org.template.word2vec

import grizzled.slf4j.Logger
import io.prediction.controller.{P2LAlgorithm, Params}

case class AlgorithmParams(mult: Int) extends Params

class Algorithm(val ap: AlgorithmParams)
  // extends PAlgorithm if Model contains RDD[]
  extends P2LAlgorithm[PreparedData, Model, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(data: PreparedData): Model = {
    // Simply count number of events
    // and multiple it by the algorithm parameter
    // and store the number as model
    val count = data.tweets.count().toInt * ap.mult
    new Model(mc = count)
  }

  def predict(model: Model, query: Query): PredictedResult = {
    // Prefix the query with the model data
    val result = s"${model.mc}-${query.q}"
    PredictedResult(p = result)
  }
}

class Model(val mc: Int)
  extends Serializable {
  override def toString = s"mc=$mc"
}
