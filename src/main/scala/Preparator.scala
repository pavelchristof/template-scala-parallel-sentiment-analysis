package org.template.sentiment

import grizzled.slf4j.Logger
import io.prediction.controller.{SanityCheck, PPreparator}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

class Preparator
  extends PPreparator[TrainingData, PreparedData] {

  def prepare(sc: SparkContext, trainingData: TrainingData): PreparedData = {
    val unlabeled = trainingData.tweets
      .sample(false, 0.001)
      .map(_.text)

    val labeled = trainingData.tweets
      .sample(false, 0.0001)

    val labels = trainingData.tweets
      .map(_.sentiment)
      .distinct
      .collect
      .toList

    PreparedData(unlabeled, labeled, labels)
  }
}

case class PreparedData(
  unlabeled: RDD[String],
  labeled: RDD[Tweet],
  labels: List[String]
) extends Serializable with SanityCheck {

  @transient lazy val logger = Logger[this.type]

  override def sanityCheck(): Unit = {
    logger.info(s"Imported ${unlabeled.count} unlabeled sentences.")
    logger.info(s"Imported ${labeled.count} labeled sentences.")
    logger.info(s"Imported ${labels.size} labels.")
  }
}
