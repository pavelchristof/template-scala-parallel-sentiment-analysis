package org.template.word2vec

import io.prediction.controller.{Engine, IEngineFactory}

case class Query(word: String, num: Int) extends Serializable

case class PredictedResult(similarWords: Array[String]) extends Serializable

object Word2VecEngine extends IEngineFactory {
  override def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("algo" -> classOf[Algorithm]),
      classOf[Serving])
  }
}
