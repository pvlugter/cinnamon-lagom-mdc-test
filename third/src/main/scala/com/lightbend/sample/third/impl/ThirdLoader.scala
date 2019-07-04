package com.lightbend.sample.third.impl

import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.sample.second.api.SecondService
import com.lightbend.sample.third.api.ThirdService
import com.softwaremill.macwire._
import java.net.URI
import play.api.libs.ws.ahc.AhcWSComponents

class ThirdLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ThirdApplication(context) with LagomDevModeComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ThirdApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[ThirdService])
}

abstract class ThirdApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  lazy val secondService = serviceClient.implement[SecondService]

  override lazy val lagomServer = serverFor[ThirdService](wire[ThirdServiceImpl])

  override lazy val jsonSerializerRegistry = ThirdSerializerRegistry

  persistentEntityRegistry.register(wire[ThirdEntity])
}
