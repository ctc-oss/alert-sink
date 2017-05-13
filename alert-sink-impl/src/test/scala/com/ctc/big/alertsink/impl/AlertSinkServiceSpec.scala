package com.ctc.big.alertsink.impl

import akka.stream.testkit.scaladsl.TestSink
import com.ctc.big.alertsink.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ServiceTest, TestTopicComponents}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class AlertSinkServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
    .withCassandra(true)
  ) { ctx =>
    new AlertSinkApplication(ctx) with LocalServiceLocator with TestTopicComponents
  }

  implicit val system = server.actorSystem
  implicit val mat = server.materializer

  val client = server.serviceClient.implement[AlertSinkService]

  override protected def afterAll() = server.stop()

  val externalEvent = ExternalEvent("foo", "bar", "baz", AlertMeta(List.empty, Coordinates("up", "down", None)))

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
        alert <- client.ingest(r.id).invoke(externalEvent)
      } yield {
        alert should matchPattern {
          case Alert(_, _, _, "bar", "foo", "baz", _) ⇒
        }
      }
    }

    "publish alerts" in {
      val source = client.alerts().subscribe.atMostOnceSource
      source.runWith(TestSink.probe[Alert])
      .request(1)
      .expectNext should matchPattern {
        case Alert(_, _, _, "bar", "foo", "baz", _) ⇒
      }
    }
  }
}
