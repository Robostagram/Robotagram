package tests

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import models._
import collection.immutable.HashMap
import models.Color

class TestBoardTemplate extends Specification{

  "should display robots in board" in {
    val robot = new Robot(Color.Blue, 0, 0)
    var robots = new HashMap[Color.Color, Robot]
    robots += ((robot.color, robot))

    val html = views.html.renderBoard(Board.boardFromFile("app/resources/Standard.board"), Some(new Goal(Color.Blue, Symbol.Moon)), robots);

    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("robot")
    contentAsString(html) must contain("Red")
    contentAsString(html) must contain("Yellow")
    contentAsString(html) must contain("Green")
    contentAsString(html) must contain("Blue")

  }
}
