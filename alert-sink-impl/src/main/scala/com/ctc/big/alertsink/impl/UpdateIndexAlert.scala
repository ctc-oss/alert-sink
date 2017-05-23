package com.ctc.big.alertsink.impl

import com.ctc.big.alertsink.api.Alert
import play.api.libs.json.{Format, Json}


case class UpdateIndexAlert(doc: Alert, doc_as_upsert: Boolean = true)

object UpdateIndexAlert {
  implicit val format: Format[UpdateIndexAlert] = Json.format
}
