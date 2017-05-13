package com.ctc.big.alertsink.impl

import java.time.LocalDateTime
import java.util.UUID

import com.ctc.big.alertsink.api.{Alert, ExternalEvent}
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
    case AlertSinkState(None) ⇒
      Actions().onCommand[RegisterApplication, String] {
        case (RegisterApplication(name), ctx, _) ⇒
          ctx.thenPersist(ApplicationRegistered(name, uuid)) { e ⇒
            ctx.reply(e.token)
          }
      }.onEvent {
        case (ApplicationRegistered(_, token), _) ⇒ AlertSinkState(Some(token))
      }

    case AlertSinkState(Some(_)) ⇒
      Actions().onCommand[GenerateAlert, Alert] {
        case (GenerateAlert(ee), ctx, _) ⇒
          val alert = Alert(uuid, entityId, LocalDateTime.now.toString, ee.url, ee.title, ee.text, ee.metadata)
          ctx.thenPersist(AlertEvent(uuid, alert)) { e ⇒
            ctx.reply(e.alert)
          }
      }.onEvent {
        case (AlertEvent(_, _), state) ⇒
          log.debug("logged alert")
          state
      }
  }

  def uuid = UUID.randomUUID.toString
}


/*
 *
 * Events
 *
 */

sealed trait AlertSinkEvent

case class ApplicationRegistered(name: String, token: String) extends AlertSinkEvent
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

case class RegisterApplication(name: String) extends AlertSinkCommand[String]
object RegisterApplication {
  implicit val format: Format[RegisterApplication] = Json.format
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

case class AlertSinkState(token: Option[String])
object AlertSinkState {
  val notready = AlertSinkState(None)
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
