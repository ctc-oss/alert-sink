package com.ctc.big.alertsink.impl

import com.ctc.big.alertsink.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class AlertSinkServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
    .withCassandra(true)
  ) { ctx =>
    new AlertSinkApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[AlertSinkService]

  override protected def afterAll() = server.stop()

  val testAlert = Alert("foo", "bar", "baz", "bash", "boom", AlertMeta(List.empty, Coordinates("up", "down", None)))

  "alert-sink service" should {

    "accept registration" in {
      client.register().invoke(Application("foo")).map { answer =>
        answer should matchPattern {
          case ApplicationRegistration(_, _) ⇒
        }
      }
    }

    "ingest alert" in {
      for {
        r <- client.register().invoke(Application("bar"))
        uuid <- client.ingest(r.id).invoke(testAlert)
      } yield {
        uuid should matchPattern {
          case _: String ⇒
        }
      }
    }
  }
}
