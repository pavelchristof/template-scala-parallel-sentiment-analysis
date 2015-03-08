package org.template.sentiment

import io.prediction.controller.{Engine, IEngineFactory}

case class Query(
  text: String
) extends Serializable

case class PredictedResult(
  probabilities: Array[(String, Double)]
) extends Serializable

object SentimentAnalysisEngine extends IEngineFactory {
  override def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("algo" -> classOf[Algorithm]),
      classOf[Serving])
  }
}
