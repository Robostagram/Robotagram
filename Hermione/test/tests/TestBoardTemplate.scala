package tests

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import models._

class TestBoardTemplate extends Specification{

  "should display robots in board" in {
    val html = views.html.board(Game.randomGame());

    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("robot")
    contentAsString(html) must contain("red")
    contentAsString(html) must contain("yellow")
    contentAsString(html) must contain("green")
    contentAsString(html) must contain("blue")

  }
}
