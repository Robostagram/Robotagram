package tests

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class TestBoard extends Specification {
  "respond to the board Action" in {
    val result = controllers.Application.board(FakeRequest())

    status(result) must equalTo(OK)
    contentType(result) must beSome("text/html")
    charset(result) must beSome("utf-8")
    contentAsString(result) must contain("<table id=\"boardGame\">")
  }
}
