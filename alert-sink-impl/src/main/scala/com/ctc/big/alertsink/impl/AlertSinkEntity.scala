/*
 * ©Concurrent Technologies Corporation 2017
 */

package com.ctc.big.alertsink.impl

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

import com.ctc.big.alertsink.api._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence._
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq


/**
 * An application has an ID and a Token
 * The ID is constant, the Token could be invalidated and refreshed
 */
class AlertSinkEntity extends PersistentEntity {
  override type Command = AlertSinkCommand[_]
  override type Event = AlertSinkEvent
  override type State = AlertSinkState

  private final val log: Logger = LoggerFactory.getLogger(classOf[AlertSinkEntity])

  override def initialState: AlertSinkState = AlertSinkState.notready

  override def behavior: Behavior = {
    case AlertSinkState(None, None, None) ⇒
      Actions().onCommand[RegisterApplication, String] {
        case (RegisterApplication(name, clss), ctx, _) ⇒
          ctx.thenPersist(ApplicationRegistered(name, uuid, clss)) { e ⇒
            ctx.reply(e.token)
          }
      }.onEvent {
        case (ApplicationRegistered(name, token, clss), _) ⇒ AlertSinkState(Some(name), Some(token), Some(clss))
      }

    case AlertSinkState(Some(name), Some(_), Some(classification)) ⇒
      Actions().onCommand[GenerateAlert, Alert] {
        case (GenerateAlert(ee), ctx, _) ⇒
          val alert = Alert(uuid, entityId, utcTimestamp(), ee.url, ee.title, ee.text, ee.classification.getOrElse(classification), ee.metadata)
          ctx.thenPersist(AlertEvent(uuid, alert)) { e ⇒
            ctx.reply(e.alert)
          }
      }.onReadOnlyCommand[AppInfo, Application] {
        case (AppInfo(), ctx, _) ⇒
          ctx.reply(Application(name, classification))
      }.onEvent {
        case (AlertEvent(_, _), state) ⇒
          log.debug(s"processed alert in {}", state.name)
          state
      }
  }

  def uuid = UUID.randomUUID.toString
  def utcTimestamp() = LocalDateTime.now.toInstant(ZoneOffset.UTC).toEpochMilli
}


/*
 *
 * Events
 *
 */

sealed trait AlertSinkEvent

case class ApplicationRegistered(name: String, token: String, classification: Classification) extends AlertSinkEvent
object ApplicationRegistered {
  implicit val format: Format[ApplicationRegistered] = Json.format
}

case class AlertEvent(uuid: String, alert: Alert) extends AlertSinkEvent with AggregateEvent[AlertEvent] {
  override def aggregateTag: AggregateEventTagger[AlertEvent] = AlertEvent.Tag
}
object AlertEvent {
  implicit val format: Format[AlertEvent] = Json.format
  val Tag = AggregateEventTag[AlertEvent]()
}

/*
 *
 * Commands
 *
 */

sealed trait AlertSinkCommand[R] extends ReplyType[R]

case class RegisterApplication(name: String, classification: Classification) extends AlertSinkCommand[String]
object RegisterApplication {
  implicit val format: Format[RegisterApplication] = Json.format
}

case class AppInfo() extends AlertSinkCommand[Application]
object AppInfo {
  implicit val format: Format[Application] = Json.format
}

case class GenerateAlert(ee: ExternalEvent) extends AlertSinkCommand[Alert]
object GenerateAlert {
  implicit val format: Format[GenerateAlert] = Json.format
}

/*
 *
 * State and registry
 *
 */

// fixme;; is this pattern right?
case class AlertSinkState(name: Option[String], token: Option[String], classification: Option[Classification])
object AlertSinkState {
  val notready = AlertSinkState(None, None, None)
  implicit val format: Format[AlertSinkState] = Json.format
}


object AlertSinkSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    // commands
    JsonSerializer[GenerateAlert],
    JsonSerializer[RegisterApplication],

    // events
    JsonSerializer[Alert],
    JsonSerializer[AlertEvent],
    JsonSerializer[ApplicationRegistered],

    // state
    JsonSerializer[AlertSinkState]
  )
}
