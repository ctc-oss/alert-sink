package com.ctc.big.alertsink.impl

import java.util.UUID

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import com.ctc.big.alertsink.api.{Alert, AlertSinkService}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


class AlertEventIndexer(sink: AlertSinkService, es: Elasticsearch)(implicit mat: ActorMaterializer, ec: ExecutionContext) extends LazyLogging {
  logger.info("Starting the Elasticsearch Alert Indexer")

  sink.alerts()
  .subscribe.withGroupId("alert-indexer").atMostOnceSource.via(
    Flow[Alert].mapAsync(1) { a ⇒
      val appQuery = sink.application(a.source).invoke()
      appQuery.onComplete {
        case Success(app) ⇒
          logger.debug("indexing {}", app)
          es.updateIndex(s"alert-${app.name}", UUID.randomUUID.toString).invoke(UpdateIndexAlert(a))

        case Failure(ex) ⇒
          logger.error(s"failed to index alert, $a", ex)
      }
      appQuery
    }
  ).runWith(Sink.foreach(println))
}
