package com.ctc.big.alertsink.impl

import akka.stream.testkit.scaladsl.TestSink
import com.ctc.big.alertsink.api._
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.{ServiceTest, TestTopicComponents}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents

class AlertSinkServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {
  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCluster(true)) { ctx ⇒
    new LagomApplication(ctx) with AlertSinkComponents with LocalServiceLocator with AhcWSComponents with TestTopicComponents
  }

  implicit val system = server.actorSystem
  implicit val mat = server.materializer

  val client = server.serviceClient.implement[AlertSinkService]

  override protected def afterAll() = server.stop()

  val externalEvent = ExternalEvent("foo", "bar", "baz", Some(Unclassified), AlertMeta(List.empty, None, None, Some(Coordinates(1, -1))))

  "alert-sink service" should {

    "accept registration" in {
      client.register().invoke(Application("foo", Unclassified)).map { answer ⇒
        answer should matchPattern {
          case ApplicationRegistration(_, _, Unclassified) ⇒
        }
      }
    }

    "ingest alert" in {
      for {
        r <- client.register().invoke(Application("bar", Unclassified))
        alert <- client.consume(r.id).invoke(externalEvent)
      } yield {
        alert should matchPattern {
          case Alert(_, _, _, "bar", "foo", "baz", Unclassified, _) ⇒
        }
      }
    }

    "publish alerts" in {
      val source = client.alerts().subscribe.atMostOnceSource
      source.runWith(TestSink.probe[Alert])
      .request(1)
      .expectNext should matchPattern {
        case Alert(_, _, _, "bar", "foo", "baz", Unclassified, _) ⇒
      }
    }
  }
}
