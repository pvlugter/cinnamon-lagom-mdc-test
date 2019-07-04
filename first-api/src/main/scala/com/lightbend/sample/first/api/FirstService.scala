package com.lightbend.sample.first.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{ KafkaProperties, PartitionKeyStrategy }
import com.lightbend.lagom.scaladsl.api.{ Service, ServiceCall }
import play.api.libs.json.{ Format, Json }

trait FirstService extends Service {

  def get(id: String): ServiceCall[NotUsed, String]

  def update(id: String): ServiceCall[NotUsed, String]

  def firstTopic: Topic[FirstMessage]

  override final def descriptor = {
    import Service._
    named("first")
      .withCalls(
        pathCall("/api/get/:id", get _),
        pathCall("/api/update/:id", update _)
      )
      .withTopics(
        topic("first", firstTopic)
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[FirstMessage](_.name)
          )
      )
      .withAutoAcl(true)
  }
}

case class FirstMessage(name: String)

object FirstMessage {
  implicit val format: Format[FirstMessage] = Json.format
}
