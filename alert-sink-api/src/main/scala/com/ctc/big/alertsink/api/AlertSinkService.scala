package com.ctc.big.alertsink.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}


/**
 * simple is as simple does
 * 1. register an application and receive a token
 * 2. submit events using that token
 * 3. events are transformed into alerts
 */
object AlertSinkService {
  val AlertsTopic = "big.alerts"
}

trait AlertSinkService extends Service {

  /**
   * @return id and token of Application
   */
  def register(): ServiceCall[Application, ApplicationRegistration]
  def application(id: String): ServiceCall[NotUsed, Application]

  /**
   * @return UUID of logged Alert as String
   */
  // fixme;; rename to "consume"?
  def ingest(id: String): ServiceCall[ExternalEvent, Alert]

  def alerts(): Topic[Alert]

  override final def descriptor = {
    import Service._
    named("alert-sink").withCalls(
      restCall(Method.POST, "/api/app", register _),
      restCall(Method.POST, "/api/alert/:id", ingest _),
      restCall(Method.GET, "/api/application/:id", application _)
    ).withTopics(
      topic(AlertSinkService.AlertsTopic, alerts _)
    ).withAutoAcl(true)
  }
}

case class Coordinates(x: String, y: String, z: Option[String])
object Coordinates {
  implicit val format: Format[Coordinates] = Json.format
}

case class AlertMeta(keywords: List[String], location: Option[String], coordinates: Option[Coordinates])
object AlertMeta {
  implicit val format: Format[AlertMeta] = Json.format
}

case class ExternalEvent(title: String, url: String, text: String, metadata: AlertMeta)
object ExternalEvent {
  implicit val format: Format[ExternalEvent] = Json.format
}

// fixme;; source is probably the appid
case class Alert(id: String, source: String, timestamp: String, url: String, title: String, text: String, metadata: AlertMeta)
object Alert {
  implicit val format: Format[Alert] = Json.format
}

case class Application(name: String)
object Application {
  implicit val format: Format[Application] = Json.format
}

case class ApplicationRegistration(id: String, token: String)
object ApplicationRegistration {
  implicit val format: Format[ApplicationRegistration] = Json.format
}
