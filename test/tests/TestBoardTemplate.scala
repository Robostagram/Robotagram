package tests

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import models._

class TestBoardTemplate extends Specification{

  "should display robots in board" in {
    val html = views.html.renderBoard(Game.randomGame());

    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("robot")
    contentAsString(html) must contain("Red")
    contentAsString(html) must contain("Yellow")
    contentAsString(html) must contain("Green")
    contentAsString(html) must contain("Blue")

  }
}
