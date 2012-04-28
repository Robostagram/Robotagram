package tests

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import models._

class TestCellTemplate extends Specification {
  "render cell template" in {
    var html = views.html.cell(Cell.Empty.withLeft(true))
    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("wall-left")

    html = views.html.cell(Cell.Empty.withRight(true))
    contentAsString(html) must contain("wall-right")

    html = views.html.cell(Cell.Empty.withTop(true))
    contentAsString(html) must contain("wall-top")

    html = views.html.cell(Cell.Empty.withBottom(true))
    contentAsString(html) must contain("wall-bottom")
  }

  "render goal in cell template" in {
    var html = views.html.cell(Cell.Empty.withGoal(new Goal(Color.Red,Symbol.Star)))

    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("red")
    contentAsString(html) must contain("star")
  }

}
