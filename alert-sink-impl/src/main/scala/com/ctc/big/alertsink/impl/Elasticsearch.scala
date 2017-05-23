package com.ctc.big.alertsink.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.Service.{named, restCall}
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.transport.Method


trait Elasticsearch extends Service {
  override def descriptor: Descriptor =
    named("elasticsearch")
    .withCalls(
      restCall(Method.POST, "/:index/items/:id/_update", updateIndex _)
      .withCircuitBreaker(CircuitBreaker.identifiedBy("elasticsearch-circuitbreaker"))
    ).withAutoAcl(true)

  def updateIndex(index: String, itemId: String): ServiceCall[UpdateIndexAlert, Done]
}
