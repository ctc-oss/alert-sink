package com.ctc.big.alertsink.impl

import java.net.URI

import akka.stream.ActorMaterializer
import com.ctc.big.alertsink.api.AlertSinkService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.client.{CircuitBreakerComponents, StaticServiceLocator}
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.db.HikariCPComponents
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext


class AlertSinkLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new AlertSinkApplication(context) {
      val esuri = elasticsearchUri
      logger.warn(s"elasticsearch static service locator uri: $esuri")

      override def serviceLocator: ServiceLocator = new StaticServiceLocator(URI.create(esuri), circuitBreakers)
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new AlertSinkApplication(context) with LagomDevModeComponents {
      val esuri = elasticsearchUri
      logger.warn(s"elasticsearch static service locator uri: $esuri")
    }

  override def describeServices = List(
    readDescriptor[AlertSinkService]
  )
}

trait AlertSinkComponents extends LagomServerComponents with JdbcPersistenceComponents with HikariCPComponents {
  implicit def executionContext: ExecutionContext

  override lazy val lagomServer = serverFor[AlertSinkService](wire[AlertSinkServiceImpl])
  override lazy val jsonSerializerRegistry = AlertSinkSerializerRegistry

  def elasticsearchUri = configuration.getString("elasticsearch.uri").getOrElse("172.17.0.1:9200")

  persistentEntityRegistry.register(wire[AlertSinkEntity])
}


abstract class AlertSinkApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
          with AlertSinkComponents
          with LagomKafkaComponents
          with CircuitBreakerComponents
          with AhcWSComponents {

  implicit val system = actorSystem
  implicit val mat = ActorMaterializer()

  lazy val elasticSearch = serviceClient.implement[Elasticsearch]
  lazy val svc = serviceClient.implement[AlertSinkService]

  wire[AlertEventIndexer]
}
