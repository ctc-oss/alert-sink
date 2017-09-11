package com.ctc.big.alertsink.api

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json


class ModelSpecs extends WordSpec with Matchers {
  val Keywords = List("foo", "Bar", "BAZ")
  val X = "100"
  val Y = "-100"
  val Location = "Jtown"

  "alert meta parsing" should {
    "handle optionals" when {
      "no location or coords" in {
        Json.parse(s"""{"keywords": ${Keywords.jsonArray}}""").as[AlertMeta] should
          matchPattern { case AlertMeta(Keywords, None, None) ⇒ }
      }
      "location only" in {
        Json.parse(s"""{"keywords": ${Keywords.jsonArray}, "location" : "$Location"}""").as[AlertMeta] should
          matchPattern { case AlertMeta(Keywords, Some(Location), None) ⇒ }

      }
      "coords only" in {
        Json.parse(s"""{"keywords": ${Keywords.jsonArray}, "coordinates": {"x": "$X", "y": "$Y"}}""").as[AlertMeta] should
          matchPattern { case AlertMeta(Keywords, None, Some(Coordinates(X, Y, None))) ⇒ }

      }
      "both location and coords" in {
        Json.parse(s"""{"keywords": ${Keywords.jsonArray}, "location" : "$Location", "coordinates": {"x": "$X", "y": "$Y"}}""").as[AlertMeta] should
          matchPattern { case AlertMeta(Keywords, Some(Location), Some(Coordinates(X, Y, None))) ⇒ }
      }
    }
  }

  implicit class FormattedList(l: List[String]) {
    def jsonArray = l.mkString("""["""", """","""", """"]""")
  }
}
