package views

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import models._

class TestCellTemplate extends Specification {
  "render cell template" in {
    var html = views.html.shared.cell(EmptyCell.withLeft(true), None, Some( new Goal(Color.Red, Symbol.ONE)), 0, 0)
    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("wall-left")

    html = views.html.shared.cell(EmptyCell.withRight(true), None, Some( new Goal(Color.Red, Symbol.ONE)), 0, 0)
    contentAsString(html) must contain("wall-right")

    html = views.html.shared.cell(EmptyCell.withTop(true), None, Some( new Goal(Color.Red, Symbol.ONE)), 0, 0)
    contentAsString(html) must contain("wall-top")

    html = views.html.shared.cell(EmptyCell.withBottom(true), None, Some( new Goal(Color.Red, Symbol.ONE)), 0, 0)
    contentAsString(html) must contain("wall-bottom")
  }

  "render red ONE in cell template" in {
    val html = views.html.shared.cell(EmptyCell.withGoal(new Goal(Color.Red, Symbol.ONE)), None, Some( new Goal(Color.Red, Symbol.ONE)), 0, 0)

    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("objective")
    contentAsString(html) must contain("Red")
    contentAsString(html) must contain("ONE")
    contentAsString(html) must not contain ("=>")
  }
  "render blue THREE in cell template" in {
    val html = views.html.shared.cell(EmptyCell.withGoal(new Goal(Color.Blue, Symbol.THREE)), None, Some( new Goal(Color.Red, Symbol.ONE)), 0, 0)

    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("Blue")
    contentAsString(html) must contain("THREE")
    contentAsString(html) must not contain ("=>")
  }
  "render yellow TWO in cell template" in {

    val html = views.html.shared.cell(EmptyCell.withGoal(new Goal(Color.Yellow, Symbol.TWO)), None, Some( new Goal(Color.Red, Symbol.ONE)), 0, 0)

    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("Yellow")
    contentAsString(html) must contain("TWO")
    contentAsString(html) must not contain ("=>")
  }
  "render green FOUR in cell template" in {
    val html = views.html.shared.cell(EmptyCell.withGoal(new Goal(Color.Green, Symbol.FOUR)), None, Some( new Goal(Color.Red, Symbol.ONE)), 0, 0)

    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("Green")
    contentAsString(html) must contain("FOUR")
    contentAsString(html) must not contain ("=>")
  }
  "render blue sun in cell template" in {
    val html = views.html.shared.cell(EmptyCell.withGoal(new Goal(Color.Blue, Symbol.FIVE)), None, Some( new Goal(Color.Red, Symbol.ONE)), 0, 0)

    contentAsString(html) must contain("symbol")
    contentAsString(html) must contain("Blue")
    contentAsString(html) must contain("FIVE")
    contentAsString(html) must not contain ("=>")
  }

  "render robot in cells" in {
    val html = views.html.shared.cell(EmptyCell, Some(new Robot(Color.Red, 0, 12)), Some( new Goal(Color.Red, Symbol.ONE)), 0, 0);
    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("robot")
    contentAsString(html) must contain("Red")
  }
  "do not render robot in cells" in {
    val html = views.html.shared.cell(EmptyCell, None, Some( new Goal(Color.Red, Symbol.ONE)), 0, 0);
    contentAsString(html) must not contain ("robot")
    contentAsString(html) must not contain ("Red")
    contentAsString(html) must not contain ("Green")
    contentAsString(html) must not contain ("Blue")
    contentAsString(html) must not contain ("Yellow")
  }

}
