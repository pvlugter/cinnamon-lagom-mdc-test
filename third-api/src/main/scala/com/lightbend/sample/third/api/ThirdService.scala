package com.lightbend.sample.third.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{ Service, ServiceCall }

trait ThirdService extends Service {

  def get(id: String): ServiceCall[NotUsed, String]

  override final def descriptor = {
    import Service._
    named("third")
      .withCalls(
        pathCall("/api/get/:id", get _)
      )
      .withAutoAcl(true)
  }
}
