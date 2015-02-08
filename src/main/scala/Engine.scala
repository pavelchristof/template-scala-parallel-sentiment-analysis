package org.template.word2vec

import io.prediction.controller.{Engine, IEngineFactory}

case class Query(q: String) extends Serializable

case class PredictedResult(p: String) extends Serializable

object Word2VecEngine extends IEngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("algo" -> classOf[Algorithm]),
      classOf[Serving])
  }
}
