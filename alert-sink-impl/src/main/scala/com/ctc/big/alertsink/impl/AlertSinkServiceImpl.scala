package com.ctc.big.alertsink.impl

import java.util.UUID

import com.ctc.big.alertsink.api.{AlertSinkService, ApplicationRegistration}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext


class AlertSinkServiceImpl(registry: PersistentEntityRegistry)(implicit ec: ExecutionContext) extends AlertSinkService {

  override def register() = ServiceCall { app =>
    val id = UUID.randomUUID.toString
    val ref = registry.refFor[AlertSinkEntity](id)
    ref.ask(RegisterApplication(app.name)).map(token ⇒ ApplicationRegistration(id, token))
  }

  override def ingest(id: String) = ServiceCall { alert =>
    val ref = registry.refFor[AlertSinkEntity](id)
    ref.ask(LogAlert(alert))
  }
}
