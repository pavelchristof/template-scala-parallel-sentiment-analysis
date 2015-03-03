package org.template.word2vec

import io.prediction.controller.{SanityCheck, PPreparator}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

class Preparator
  extends PPreparator[TrainingData, PreparedData] {

  def prepare(sc: SparkContext, trainingData: TrainingData): PreparedData = {
    PreparedData(trainingData.tweets.map(_.text).sample(false, 0.1))
  }
}

case class PreparedData(sentences: RDD[String])
  extends Serializable with SanityCheck {
  override def sanityCheck(): Unit = {
    printf("Imported %d sentences.", sentences.count())
  }
}