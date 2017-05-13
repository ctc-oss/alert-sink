package com.ctc.big.alertsink.impl

import java.util.UUID

import com.ctc.big.alertsink.api.{Alert, AlertSinkService, ApplicationRegistration}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext


class AlertSinkServiceImpl(registry: PersistentEntityRegistry)(implicit ec: ExecutionContext) extends AlertSinkService {

  override def register() = ServiceCall { app =>
    val id = UUID.randomUUID.toString
    val ref = registry.refFor[AlertSinkEntity](id)
    ref.ask(RegisterApplication(app.name)).map(token ⇒ ApplicationRegistration(id, token))
  }

  override def ingest(id: String) = ServiceCall { ee =>
    val ref = registry.refFor[AlertSinkEntity](id)
    ref.ask(GenerateAlert(ee))
  }

  override def alerts(): Topic[Alert] = {
    TopicProducer.singleStreamWithOffset { offset ⇒
      registry.eventStream(AlertEvent.Tag, offset)
      .map(ev ⇒ ae2a2(ev.event) → offset)
    }
  }

  private def ae2a2(a: AlertEvent): Alert = Alert(
    a.uuid,
    a.alert.source,
    a.alert.timestamp,
    a.alert.url,
    a.alert.title,
    a.alert.text,
    a.alert.metadata
  )
}
