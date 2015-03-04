package org.template.sentiment

import grizzled.slf4j.Logger
import io.prediction.controller._
import io.prediction.data.storage.Storage
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

case class DataSourceParams(appId: Int) extends Params

class DataSource(val dsp: DataSourceParams)
  extends PDataSource[TrainingData,
      EmptyEvaluationInfo, Query, EmptyActualResult] {

  @transient lazy val logger = Logger[this.type]

  override
  def readTraining(sc: SparkContext): TrainingData = {
    val eventsDb = Storage.getPEvents()
    val tweets = eventsDb
      .find(
        appId = dsp.appId,
        entityType = Some("source"),
        eventNames = Some(List("tweet"))
      )(sc)
      .map(e => Tweet(
      e.properties.get[String]("text"),
      e.properties.get[String]("sentiment")
    ))

    new TrainingData(tweets)
  }
}

case class Tweet(text: String,
                 sentiment: String)
  extends Serializable

class TrainingData(
  val tweets: RDD[Tweet]
) extends Serializable
