package org.template.sentiment

import grizzled.slf4j.Logger
import io.prediction.controller._
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

case class PreparatorParams(
  val unlabeledSample: Double,
  val labeledSample: Double
) extends Params

class Preparator(val params: PreparatorParams)
  extends PPreparator[TrainingData, PreparedData] {

  @transient lazy val logger = Logger[this.type]

  def prepare(sc: SparkContext, trainingData: TrainingData): PreparedData = {
    trainingData.tweets.cache()

    val unlabeled = trainingData.tweets
      .sample(false, params.unlabeledSample)
      .map(_.text)

    val labeled = trainingData.tweets
      .sample(false, params.labeledSample)

    val labels = trainingData.tweets
      .map(_.sentiment)
      .distinct
      .collect
      .toList

    logger.info(s"Imported ${unlabeled.count} unlabeled sentences.")
    logger.info(s"Imported ${labeled.count} labeled sentences.")
    logger.info(s"Imported ${labels.size} labels.")

    PreparedData(unlabeled, labeled, labels)
  }
}

case class PreparedData(
  unlabeled: RDD[String],
  labeled: RDD[Tweet],
  labels: List[String]
) extends Serializable
