package com.ctc.big.alertsink.api

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsResultException, Json}


class ModelSpecs extends WordSpec with Matchers {
  val Title = "Awesome Alert"
  val Text = "text!"
  val Url = "ctc.com"
  val Keywords = List("foo", "Bar", "BAZ")
  val X = "100"
  val Y = "-100"
  val Location = "Jtown"
  val Classification = Unclassified

  "alert meta parsing" should {
    "handle optionals" when {
      "no location or coords" in {
        Json.parse(s"""{"keywords": ${Keywords.asjson}}""").as[AlertMeta] should
          matchPattern { case AlertMeta(Keywords, None, None) ⇒ }
      }
      "location only" in {
        Json.parse(s"""{"classification":${Classification.asjson}, "keywords": ${Keywords.asjson}, "location" : "$Location"}""").as[AlertMeta] should
          matchPattern { case AlertMeta(Keywords, Some(Location), None) ⇒ }

      }
      "coords only" in {
        Json.parse(s"""{"classification":${Classification.asjson}, "keywords": ${Keywords.asjson}, "coordinates": {"x": "$X", "y": "$Y"}}""").as[AlertMeta] should
          matchPattern { case AlertMeta(Keywords, None, Some(Coordinates(X, Y, None))) ⇒ }

      }
      "both location and coords" in {
        Json.parse(s"""{"classification":${Classification.asjson}, "keywords": ${Keywords.asjson}, "location" : "$Location", "coordinates": {"x": "$X", "y": "$Y"}}""").as[AlertMeta] should
          matchPattern { case AlertMeta(Keywords, Some(Location), Some(Coordinates(X, Y, None))) ⇒ }
      }
    }
  }

  "external event parsing" should {
    "handle classification" when {
      "unclassified" in {
        Json.parse(s"""{"classification":${Unclassified.asjson}, "title":"$Title","url":"$Url","text":"$Text","metadata":{"keywords": ${Keywords.asjson}}}""").as[ExternalEvent] should
          matchPattern { case ExternalEvent(Title, Url, Text, Some(Unclassified), _) ⇒ }
      }
      "classified" in {
        Json.parse(s"""{"classification":${Classified.asjson}, "title":"$Title","url":"$Url","text":"$Text","metadata":{"keywords": ${Keywords.asjson}}}""").as[ExternalEvent] should
          matchPattern { case ExternalEvent(Title, Url, Text, Some(Classified), _) ⇒ }
      }
      "mixed case" in {
        Json.parse(s"""{"classification":"UnClAsSiFiEd", "title":"$Title","url":"$Url","text":"$Text","metadata":{"keywords": ${Keywords.asjson}}}""").as[ExternalEvent] should
          matchPattern { case ExternalEvent(Title, Url, Text, Some(Unclassified), _) ⇒ }
      }
      "invalid" in {
        intercept[JsResultException] {
          Json.parse(s"""{"classification":"foo", "title":"$Title","url":"$Url","text":"$Text","metadata":{"keywords": ${Keywords.asjson}}}""").as[ExternalEvent]
        }
      }
    }
  }

  implicit class FormattedList(l: List[String]) {
    def asjson = l.mkString("""["""", """","""", """"]""")
  }

  implicit class FormattedClassification(c: Classification) {
    def asjson = {
      s""""${c.toString}""""
    }
  }
}
