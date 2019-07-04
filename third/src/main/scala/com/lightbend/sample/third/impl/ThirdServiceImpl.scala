package com.lightbend.sample.third.impl

import akka.Done
import akka.stream.scaladsl.Flow
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.sample.second.api.{ SecondMessage, SecondService }
import com.lightbend.sample.third.api.ThirdService
import org.slf4j.{ LoggerFactory, MDC }
import scala.concurrent.ExecutionContext

class ThirdServiceImpl(persistentEntityRegistry: PersistentEntityRegistry, secondService: SecondService)(implicit ec: ExecutionContext) extends ThirdService {
  val log = LoggerFactory.getLogger(getClass)

  secondService.secondTopic.subscribe.atLeastOnce(
    Flow[SecondMessage].mapAsync(1) {
      case SecondMessage(id) =>
        MDC.put("id", id)
        val ref = persistentEntityRegistry.refFor[ThirdEntity](id)
        ref.ask(UpdateThird(id))
    }.map(_ => Done)
  )

  override def get(id: String) = ServiceCall { _ =>
    MDC.put("id", id)
    val ref = persistentEntityRegistry.refFor[ThirdEntity](id)
    ref.ask(GetThird(id))
  }
}
