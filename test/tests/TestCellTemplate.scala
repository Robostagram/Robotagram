package tests

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import models._

class TestCellTemplate extends Specification {
  "render cell template" in {
    var html = views.html.cell(EmptyCell.withLeft(true), None, Some( new Goal(Color.Red, Symbol.Star)), 0, 0)
    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("wall-left")

    html = views.html.cell(EmptyCell.withRight(true), None, Some( new Goal(Color.Red, Symbol.Star)), 0, 0)
    contentAsString(html) must contain("wall-right")

    html = views.html.cell(EmptyCell.withTop(true), None, Some( new Goal(Color.Red, Symbol.Star)), 0, 0)
    contentAsString(html) must contain("wall-top")

    html = views.html.cell(EmptyCell.withBottom(true), None, Some( new Goal(Color.Red, Symbol.Star)), 0, 0)
    contentAsString(html) must contain("wall-bottom")
  }

  "render red star in cell template" in {
    val html = views.html.cell(EmptyCell.withGoal(new Goal(Color.Red, Symbol.Star)), None, Some( new Goal(Color.Red, Symbol.Star)), 0, 0)

    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("objective")
    contentAsString(html) must contain("Red")
    contentAsString(html) must contain("Star")
    contentAsString(html) must not contain ("=>")
  }
  "render blue gear in cell template" in {
    val html = views.html.cell(EmptyCell.withGoal(new Goal(Color.Blue, Symbol.Gear)), None, Some( new Goal(Color.Red, Symbol.Star)), 0, 0)

    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("Blue")
    contentAsString(html) must contain("Gear")
    contentAsString(html) must not contain ("=>")
  }
  "render yellow planet in cell template" in {

    val html = views.html.cell(EmptyCell.withGoal(new Goal(Color.Yellow, Symbol.Planet)), None, Some( new Goal(Color.Red, Symbol.Star)), 0, 0)

    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("Yellow")
    contentAsString(html) must contain("Planet")
    contentAsString(html) must not contain ("=>")
  }
  "render green moon in cell template" in {
    val html = views.html.cell(EmptyCell.withGoal(new Goal(Color.Green, Symbol.Moon)), None, Some( new Goal(Color.Red, Symbol.Star)), 0, 0)

    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("Green")
    contentAsString(html) must contain("Moon")
    contentAsString(html) must not contain ("=>")
  }
  "render blue sun in cell template" in {
    val html = views.html.cell(EmptyCell.withGoal(new Goal(Color.Blue, Symbol.Sun)), None, Some( new Goal(Color.Red, Symbol.Star)), 0, 0)

    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("Blue")
    contentAsString(html) must contain("Sun")
    contentAsString(html) must not contain ("=>")
  }

  "render robot in cells" in {
    val html = views.html.cell(EmptyCell, Some(new Robot(Color.Red, 0, 12)), Some( new Goal(Color.Red, Symbol.Star)), 0, 0);
    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("robot")
    contentAsString(html) must contain("Red")
  }
  "do not render robot in cells" in {
    val html = views.html.cell(EmptyCell, None, Some( new Goal(Color.Red, Symbol.Star)), 0, 0);
    contentAsString(html) must not contain ("robot")
    contentAsString(html) must not contain ("Red")
    contentAsString(html) must not contain ("Green")
    contentAsString(html) must not contain ("Blue")
    contentAsString(html) must not contain ("Yellow")
  }

}
