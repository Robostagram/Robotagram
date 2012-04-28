package tests

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import models._

class TestCellTemplate extends Specification {
  "render cell template" in {
    var html = views.html.cell(EmptyCell.withLeft(true),null)
    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("wall-left")

    html = views.html.cell(EmptyCell.withRight(true),null)
    contentAsString(html) must contain("wall-right")

    html = views.html.cell(EmptyCell.withTop(true),null)
    contentAsString(html) must contain("wall-top")

    html = views.html.cell(EmptyCell.withBottom(true),null)
    contentAsString(html) must contain("wall-bottom")
  }

  "render red star in cell template" in {
    val html = views.html.cell(EmptyCell.withGoal(new Goal(Color.Red,Symbol.Star)),null)

    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("red")
    contentAsString(html) must contain("star")
    contentAsString(html) must not contain("=>")
  }
  "render blue gear in cell template" in {
    val html = views.html.cell(EmptyCell.withGoal(new Goal(Color.Blue,Symbol.Gear)),null)

    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("blue")
    contentAsString(html) must contain("gear")
    contentAsString(html) must not contain("=>")
  }
  "render yellow planet in cell template" in {

    val html = views.html.cell(EmptyCell.withGoal(new Goal(Color.Yellow,Symbol.Planet)),null)

    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("yellow")
    contentAsString(html) must contain("planet")
    contentAsString(html) must not contain("=>")
  }
  "render green moon in cell template" in {
    val html = views.html.cell(EmptyCell.withGoal(new Goal(Color.Green,Symbol.Moon)),null)

    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("green")
    contentAsString(html) must contain("moon")
    contentAsString(html) must not contain("=>")
  }
  "render blue sun in cell template" in {
    val html = views.html.cell(EmptyCell.withGoal(new Goal(Color.Blue,Symbol.Sun)),null)

    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("blue")
    contentAsString(html) must contain("sun")
    contentAsString(html) must not contain("=>")
  }

  "render robot in cells" in {
    val html = views.html.cell(EmptyCell,new Robot(Color.Red));
    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("robot")
    contentAsString(html) must contain("red")
  }
  "do not render robot in cells" in {
    val html = views.html.cell(EmptyCell,null);
    contentAsString(html) must not contain("robot")
    contentAsString(html) must not contain("red")
    contentAsString(html) must not contain("green")
    contentAsString(html) must not contain("blue")
    contentAsString(html) must not contain("yellow")
  }

}
