package org.template.word2vec

import io.prediction.controller.PPreparator
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

class Preparator
  extends PPreparator[TrainingData, PreparedData] {

  def prepare(sc: SparkContext, trainingData: TrainingData): PreparedData = {
    PreparedData(tweets = trainingData.tweets)
  }
}

case class PreparedData(tweets: RDD[Tweet])
  extends Serializable
