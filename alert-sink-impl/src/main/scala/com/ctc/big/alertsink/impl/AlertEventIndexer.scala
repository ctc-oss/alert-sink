package com.ctc.big.alertsink.impl

import java.util.UUID

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import com.ctc.big.alertsink.api.{Alert, AlertSinkService}

import scala.concurrent.ExecutionContext


class AlertEventIndexer(svc: AlertSinkService, es: Elasticsearch)(implicit mat: ActorMaterializer, ec: ExecutionContext) {
  svc.alerts()
  .subscribe.withGroupId("alert-indexer").atMostOnceSource.via(
    Flow[Alert].alsoTo(Sink.foreach(a ⇒ println(s"indexing: $a"))).mapAsync(1) { a ⇒
      es.updateIndex("alert", UUID.randomUUID.toString).invoke(UpdateIndexAlert(a))
    }
  ).runWith(Sink.foreach(println))
}
