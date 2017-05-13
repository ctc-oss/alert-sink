package com.ctc.big.alertsink.impl

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.ctc.big.alertsink.api._
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class AlertSinkEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("AlertSinkEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(AlertSinkSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[AlertSinkCommand[_], AlertSinkEvent, AlertSinkState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new AlertSinkEntity {
      override def uuid = TestUUID
    }, EntityID)

    block(driver)
    driver.getAllIssues should have size 0
  }

  val EntityID = UUID.randomUUID.toString.take(7)
  val TestUUID = UUID.randomUUID.toString.take(7)
  val externalEvent = ExternalEvent("foo", "bar", "baz", AlertMeta(List.empty, Coordinates("up", "down", None)))

  "alert-sink entity" should {
    "accept application registration" in withTestDriver { driver =>
      val outcome = driver.run(RegisterApplication("test"))
      outcome.replies should contain only TestUUID
    }

    "log an event to existing application" in withTestDriver { driver =>
      val outcome1 = driver.run(RegisterApplication("test"))
      outcome1.replies should contain only TestUUID
      val outcome2 = driver.run(GenerateAlert(externalEvent))
      outcome2.events.head should matchPattern {
        case AlertEvent(TestUUID, Alert(TestUUID, EntityID, _, "bar", "foo", "baz", _)) ⇒
      }
    }
  }
}
