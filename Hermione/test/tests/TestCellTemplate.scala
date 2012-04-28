package tests

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import models.Cell

class TestCellTemplate extends Specification {
  "render cell template" in {
    var html = views.html.cell(Cell.Empty.withLeft(true))
    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("wall-left")

    html = views.html.cell(Cell.Empty.withRight(true))
    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("wall-right")

    html = views.html.cell(Cell.Empty.withTop(true))
    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("wall-top")

    html = views.html.cell(Cell.Empty.withBottom(true))
    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("wall-bottom")
  }

}
