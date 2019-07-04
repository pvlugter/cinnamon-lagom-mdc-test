package com.lightbend.sample.second.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{ KafkaProperties, PartitionKeyStrategy }
import com.lightbend.lagom.scaladsl.api.{ Service, ServiceCall }
import play.api.libs.json.{ Format, Json }

trait SecondService extends Service {

  def get(id: String): ServiceCall[NotUsed, String]

  def secondTopic: Topic[SecondMessage]

  override final def descriptor = {
    import Service._
    named("second")
      .withCalls(
        pathCall("/api/get/:id", get _)
      )
      .withTopics(
        topic("second", secondTopic)
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[SecondMessage](_.name)
          )
      )
      .withAutoAcl(true)
  }
}

case class SecondMessage(name: String)

object SecondMessage {
  implicit val format: Format[SecondMessage] = Json.format
}
