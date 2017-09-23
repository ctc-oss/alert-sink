package com.ctc.big.alertsink.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json._

import scala.util.Try


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
  def consume(id: String): ServiceCall[ExternalEvent, Alert]

  def alerts(): Topic[Alert]

  override final def descriptor = {
    import Service._
    named("alert-sink").withCalls(
      restCall(Method.POST, "/api/app", register _),
      restCall(Method.POST, "/api/alert/:id", consume _),
      restCall(Method.GET, "/api/application/:id", application _)
    ).withTopics(
      topic(AlertSinkService.AlertsTopic, alerts _)
    ).withAutoAcl(true)
  }
}

sealed trait Classification
case object Unclassified extends Classification
case object Classified extends Classification

object Classification {
  implicit val format: Format[Classification] = new Format[Classification] {
    def writes(o: Classification) = JsString(o.toString.toLowerCase)
    def reads(json: JsValue) = json match {
      case JsString(v) ⇒ v.toLowerCase match {
        case "unclassified" ⇒ JsSuccess(Unclassified)
        case "classified" ⇒ JsSuccess(Classified)
        case _ ⇒ JsError(s"failed to parse Classification from $v")
      }
      case _ ⇒ JsError(s"expected string Classification marking")
    }
  }
}

case class Coordinates(lon: Double, lat: Double)
object Coordinates {
  def parse(slon: String, slat: String): Option[Coordinates] = {
    for {
      lon ← Try(slon.toDouble).toOption
      lat ← Try(slat.toDouble).toOption
    } yield Coordinates(lon, lat)
  }

  implicit val format: Format[Coordinates] = Json.format
}

case class AlertMeta(keywords: List[String], eventTime: Option[Long], locationName: Option[String], location: Option[Coordinates])
object AlertMeta {
  implicit val format: Format[AlertMeta] = Json.format
}

case class ExternalEvent(title: String, url: String, text: String, classification: Option[Classification], metadata: AlertMeta)
object ExternalEvent {
  implicit val format: Format[ExternalEvent] = Json.format
}

case class Alert(id: String, source: String, alertTime: Long, url: String, title: String, text: String, classification: Classification, metadata: AlertMeta)
object Alert {
  implicit val format: Format[Alert] = Json.format
}

case class Application(name: String, classification: Classification)
object Application {
  implicit val format: Format[Application] = Json.format
}

case class ApplicationRegistration(id: String, token: String, classification: Classification)
object ApplicationRegistration {
  implicit val format: Format[ApplicationRegistration] = Json.format
}
