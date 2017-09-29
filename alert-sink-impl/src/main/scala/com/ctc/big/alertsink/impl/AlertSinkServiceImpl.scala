/*
 * ©Concurrent Technologies Corporation 2017
 */

package com.ctc.big.alertsink.impl

import java.util.UUID

import com.ctc.big.alertsink.api._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext


class AlertSinkServiceImpl(registry: PersistentEntityRegistry)(implicit ec: ExecutionContext) extends AlertSinkService with LazyLogging {

  override def register() = ServiceCall { app ⇒
    if (app.name.isEmpty) logger.warn("application name is empty")

    val id = UUID.randomUUID.toString
    val ref = registry.refFor[AlertSinkEntity](id)
    ref.ask(RegisterApplication(app.name, app.classification)).map { token ⇒
      logger.info("application {} registered", app.name)
      ApplicationRegistration(id, token, app.classification)
    }
  }


  def application(id: String) = ServiceCall { _ ⇒
    val ref = registry.refFor[AlertSinkEntity](id)
    ref.ask(AppInfo())
  }

  override def consume(id: String) = ServiceCall { ee ⇒
    val ref = registry.refFor[AlertSinkEntity](id)
    ref.ask(GenerateAlert(ee)).recover{
      case ex ⇒ throw new Exception(s"${ee.title} failed with ${ex.getMessage}")
    }
  }

  override def alerts(): Topic[Alert] = {
    TopicProducer.singleStreamWithOffset { offset ⇒
      registry.eventStream(AlertEvent.Tag, offset).map(ev ⇒ ev.event.alert → ev.offset)
    }
  }
}
