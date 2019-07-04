package com.lightbend.sample.first.impl

import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.sample.first.api.FirstService
import com.softwaremill.macwire._
import java.net.URI
import play.api.libs.ws.ahc.AhcWSComponents

class FirstLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new FirstApplication(context) with LagomDevModeComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new FirstApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[FirstService])
}

abstract class FirstApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[FirstService](wire[FirstServiceImpl])

  override lazy val jsonSerializerRegistry = FirstSerializerRegistry

  persistentEntityRegistry.register(wire[FirstEntity])
}
