package com.lightbend.sample.second.impl

import akka.Done
import akka.stream.scaladsl.Flow
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{ EventStreamElement, PersistentEntityRegistry }
import com.lightbend.sample.first.api.{ FirstMessage, FirstService }
import com.lightbend.sample.second.api.{ SecondMessage, SecondService }
import org.slf4j.{ LoggerFactory, MDC }
import scala.concurrent.ExecutionContext

class SecondServiceImpl(persistentEntityRegistry: PersistentEntityRegistry, firstService: FirstService)(implicit ec: ExecutionContext) extends SecondService {
  val log = LoggerFactory.getLogger(getClass)

  firstService.firstTopic.subscribe.atLeastOnce(
    Flow[FirstMessage].mapAsync(1) {
      case FirstMessage(id) =>
        MDC.put("id", id)
        val ref = persistentEntityRegistry.refFor[SecondEntity](id)
        ref.ask(UpdateSecond(id))
    }.map(_ => Done)
  )

  override def get(id: String) = ServiceCall { _ =>
    MDC.put("id", id)
    val ref = persistentEntityRegistry.refFor[SecondEntity](id)
    ref.ask(GetSecond(id))
  }

  override def secondTopic: Topic[SecondMessage] = TopicProducer.singleStreamWithOffset { fromOffset =>
    persistentEntityRegistry.eventStream(SecondEvent.Tag, fromOffset).map {
      case EventStreamElement(_, SecondUpdated(id), offset) =>
        log.info(s"$id - message")
        if (MDC.get("id") ne null) log.error(s"!!! unexpected MDC")
        SecondMessage(id) -> offset
    }
  }
}
