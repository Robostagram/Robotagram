import models.EmptyCell
import org.specs2.mutable._

import org.specs2.specification.Scope

class EmptyCellSpec extends Specification {
  "The empty cell" should {
    "have no walls" in new WithEmptyCell {
      cell.wallTop must beFalse
      cell.wallBottom must beFalse
      cell.wallLeft must beFalse
      cell.wallRight must beFalse
    }
    "have no goal" in new WithEmptyCell {
      cell.goal
    }
  }
}

trait WithEmptyCell extends Scope {
  val cell = EmptyCell
}