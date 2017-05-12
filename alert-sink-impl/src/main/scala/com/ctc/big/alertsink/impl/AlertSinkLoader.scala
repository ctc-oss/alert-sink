package com.ctc.big.alertsink.impl

import com.ctc.big.alertsink.api.AlertSinkService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class AlertSinkLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new AlertSinkApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new AlertSinkApplication(context) with LagomDevModeComponents

  override def describeServices = List(
    readDescriptor[AlertSinkService]
  )
}

abstract class AlertSinkApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
          with CassandraPersistenceComponents
          with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[AlertSinkService](wire[AlertSinkServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = AlertSinkSerializerRegistry

  // Register the alert-sink persistent entity
  persistentEntityRegistry.register(wire[AlertSinkEntity])
}
