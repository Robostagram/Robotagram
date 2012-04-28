package tests

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import models._

class TestBoardTemplate extends Specification{

  "should display robots in board" in {
    val robots:Array[Robot] =   Array(new Robot(Color.Red),new Robot(Color.Blue),new Robot(Color.Yellow),new Robot(Color.Green))
    val html = views.html.board(new Board(16,16),robots);

    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("robot")
    contentAsString(html) must contain("red")
    contentAsString(html) must contain("yellow")
    contentAsString(html) must contain("green")
    contentAsString(html) must contain("blue")

  }
}
