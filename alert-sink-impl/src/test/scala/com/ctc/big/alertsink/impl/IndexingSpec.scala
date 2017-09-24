package com.ctc.big.alertsink.impl

import java.time.Instant

import com.ctc.big.alertsink.api.{Alert, AlertMeta, Unclassified}
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json

class IndexingSpec extends WordSpec with Matchers{
  "elastic indexing" should {
    "write time as string" in {
      //  Alert(id: String, source: String, time: Long, url: String, title: String, text: String, classification: Classification, metadata: AlertMeta)
      val t = 1
      val a = Alert("foo", "bar", t, "baz", "biz", "bop", Unclassified, AlertMeta.Empty)
      val i = UpdateIndexAlert(a)

      Json.toJson(i) shouldBe Json.parse(s"""{"doc":{"id":"foo", "source":"bar", "time":"${Instant.ofEpochMilli(1).toString}", "url":"baz", "title":"biz", "text":"bop", "classification":"unclassified", "metadata":{"keywords":[]}},"doc_as_upsert":true}""")
    }
  }
}
