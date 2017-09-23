package com.ctc.big.alertsink.api

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsResultException, Json}


class ModelSpecs extends WordSpec with Matchers {
  val Title = "Awesome Alert"
  val Text = "text!"
  val Url = "ctc.com"
  val Time = System.currentTimeMillis
  val Keywords = List("foo", "Bar", "BAZ")
  val X = "100"
  val Y = "-100"
  val Coords = Coordinates.parse(X, Y)
  val LocationName = "Jtown"
  val Classification = Unclassified

  "alert meta parsing" should {
    "handle optionals" when {
      "no location or coords" in {
        Json.parse(s"""{"keywords": ${Keywords.asjson}}""").as[AlertMeta] should
          matchPattern { case AlertMeta(Keywords, None, None, None) ⇒ }
      }
      "location only" in {
        Json.parse(s"""{"classification":${Classification.asjson}, "keywords": ${Keywords.asjson}, "locationName" : "$LocationName"}""").as[AlertMeta] should
          matchPattern { case AlertMeta(Keywords, None, Some(LocationName), None) ⇒ }

      }
      "coords only" in {
        Json.parse(s"""{"classification":${Classification.asjson}, "keywords": ${Keywords.asjson}, "location": {"lat": $Y, "lon": $X}}""").as[AlertMeta] should
          matchPattern { case AlertMeta(Keywords, None, None, Coords) ⇒ }

      }
      "both locationName and location with time" in {
        Json.parse(s"""{"classification":${Classification.asjson}, "eventTime":$Time, "keywords": ${Keywords.asjson}, "locationName" : "$LocationName", "location": {"lat": $Y, "lon": $X}}""").as[AlertMeta] should
          matchPattern { case AlertMeta(Keywords, Some(Time), Some(LocationName), Coords) ⇒ }
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
