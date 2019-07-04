package com.lightbend.sample.first.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{ EventStreamElement, PersistentEntityRegistry }
import com.lightbend.sample.first.api.{ FirstMessage, FirstService }
import org.slf4j.{ LoggerFactory, MDC }
import scala.concurrent.ExecutionContext

class FirstServiceImpl(persistentEntityRegistry: PersistentEntityRegistry)(implicit ec: ExecutionContext) extends FirstService {
  val log = LoggerFactory.getLogger(getClass)

  override def get(id: String) = ServiceCall { _ =>
    MDC.put("id", id)
    val ref = persistentEntityRegistry.refFor[FirstEntity](id)
    ref.ask(GetFirst(id))
  }

  override def update(id: String) = ServiceCall { _ =>
    MDC.put("id", id)
    val ref = persistentEntityRegistry.refFor[FirstEntity](id)
    ref.ask(UpdateFirst(id))
  }

  override def firstTopic: Topic[FirstMessage] = TopicProducer.singleStreamWithOffset { fromOffset =>
    persistentEntityRegistry.eventStream(FirstEvent.Tag, fromOffset).map {
      case EventStreamElement(_, FirstUpdated(id), offset) =>
        log.info(s"$id - message")
        if (MDC.get("id") ne null) log.error(s"!!! unexpected MDC")
        FirstMessage(id) -> offset
    }
  }
}
