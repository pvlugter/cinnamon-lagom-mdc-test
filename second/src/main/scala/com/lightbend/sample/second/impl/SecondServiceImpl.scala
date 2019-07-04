package com.lightbend.sample.second.impl

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{ EventStreamElement, PersistentEntityRegistry }
import com.lightbend.sample.first.api.{ FirstMessage, FirstService }
import com.lightbend.sample.second.api.{ SecondMessage, SecondService }
import org.slf4j.{ LoggerFactory, MDC }
import scala.concurrent.ExecutionContext

class SecondServiceImpl(persistentEntityRegistry: PersistentEntityRegistry, firstService: FirstService, system: ActorSystem)(implicit ec: ExecutionContext) extends SecondService {
  val log = LoggerFactory.getLogger(getClass)

  firstService.firstTopic.subscribe.atLeastOnce(
    Flow[FirstMessage].mapAsync(1) {
      case FirstMessage(id) =>
        MDC.put("id", id)
        // force a residual MDC on to the lagom persistence dispatcher (single thread)
        system.dispatchers.lookup("lagom.persistence.dispatcher").execute(new Runnable {
          def run = MDC.put("id", "residual-" + id)
        })
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
        if (MDC.get("id") ne null) {
          log.error(s"!!! Unexpected MDC with origin = " + MDC.get("cinnamon.debug.origin"))
          // log.error(s"!!! Unexpected MDC with stacktraces = " + MDC.get("cinnamon.debug.stacktraces"))
        }
        SecondMessage(id) -> offset
    }
  }
}
