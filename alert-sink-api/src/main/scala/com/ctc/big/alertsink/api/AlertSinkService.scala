package com.ctc.big.alertsink.api

import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}


/**
 * simple is as simple does
 * 1. register an application and receive a token
 * 2. submit events using that token
 */
trait AlertSinkService extends Service {

  /**
   * @return id and token of application
   */
  def register(): ServiceCall[Application, ApplicationRegistration]

  /**
   * @return UUID of logged alert as String
   */
  def ingest(id: String): ServiceCall[Alert, String]

  override final def descriptor = {
    import Service._
    named("alert-sink").withCalls(
      restCall(Method.POST, "/api/app", register _),
      restCall(Method.POST, "/api/alert/:id", ingest _)
    ).withAutoAcl(true)
  }
}

case class Coordinates(x: String, y: String, z: Option[String])
object Coordinates {
  implicit val format: Format[Coordinates] = Json.format
}

case class AlertMeta(keywords: List[String], coordinates: Coordinates)
object AlertMeta {
  implicit val format: Format[AlertMeta] = Json.format
}

case class Alert(source: String, timestamp: String, url: String, title: String, text: String, metadata: AlertMeta)
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
