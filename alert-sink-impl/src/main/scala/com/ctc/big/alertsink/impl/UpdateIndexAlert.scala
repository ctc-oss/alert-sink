package com.ctc.big.alertsink.impl

import java.time.Instant

import com.ctc.big.alertsink.api.Alert
import play.api.libs.json._


case class UpdateIndexAlert(doc: Alert, doc_as_upsert: Boolean = true)

object UpdateIndexAlert {
  private val IndexedDocumentField = "doc"
  private val AlertTimeField = "time"

  /**
   * the intent here is to keep the API on a Long timestamp while converting to String for elasticsearch when indexing
   * to do so we will use the default formatter and rewrite the time field as a string date that es will pick up as a timestamp
   * ... there very well may be a more elegant way to do this in play json ...
   */
  implicit val format = Format(Json.reads[UpdateIndexAlert], new OWrites[UpdateIndexAlert] {
    def writes(a: UpdateIndexAlert): JsObject = {
      val o = Json.writes.writes(a)
      o.value.get(IndexedDocumentField).flatMap {
        case JsObject(doc) ⇒ doc.get(AlertTimeField).flatMap {
          case JsNumber(v) ⇒ Some(JsString(Instant.ofEpochMilli(v.toLong).toString))
          case _ ⇒ None
        }.map(t ⇒ doc.updated(AlertTimeField, t)).map(JsObject(_))
        case _ ⇒ None
      }.map(doc ⇒ JsObject(o.value.updated(IndexedDocumentField, doc))).getOrElse(o)
    }
  })
}
