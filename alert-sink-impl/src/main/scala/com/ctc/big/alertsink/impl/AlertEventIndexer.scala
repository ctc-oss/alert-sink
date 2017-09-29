/*
 * ©Concurrent Technologies Corporation 2017
 */

package com.ctc.big.alertsink.impl

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import com.ctc.big.alertsink.api.{Alert, AlertSinkService}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext


class AlertEventIndexer(sink: AlertSinkService, es: Elasticsearch)(implicit mat: ActorMaterializer, ec: ExecutionContext) extends LazyLogging {
  logger.info("Starting the Elasticsearch Alert Indexer")

  sink.alerts()
  .subscribe.withGroupId("alert-indexer").atMostOnceSource.via(
    Flow[Alert].mapAsync(1)(alert ⇒
      sink.application(alert.source).invoke().map(_ → alert)
    ).mapAsync(1) { aa ⇒
      es.updateIndex(s"alert-${aa._1.name}", aa._2.id)
      .invoke(UpdateIndexAlert(aa._2))
      .recover {
        case ex ⇒ logger.warn("failed to index alert [{}], {}", aa._2.id, ex.getMessage)
      }
      .map(_ ⇒ aa)
    }
  ).runWith(Sink.foreach(aa ⇒
    logger.debug("indexed alert [{}] to app {}", aa._2.id, aa._1.name)
  ))
}
