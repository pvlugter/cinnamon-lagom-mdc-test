package com.lightbend.sample.second.impl

import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.sample.first.api.FirstService
import com.lightbend.sample.second.api.SecondService
import com.softwaremill.macwire._
import java.net.URI
import play.api.libs.ws.ahc.AhcWSComponents

class SecondLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SecondApplication(context) with LagomDevModeComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SecondApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[SecondService])
}

abstract class SecondApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  lazy val firstService = serviceClient.implement[FirstService]

  override lazy val lagomServer = serverFor[SecondService](wire[SecondServiceImpl])

  override lazy val jsonSerializerRegistry = SecondSerializerRegistry

  persistentEntityRegistry.register(wire[SecondEntity])
}
