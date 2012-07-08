package tests

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

trait EmptyBoardFromFile extends Scope {
  val originalBoardString = scala.io.Source.fromFile("app/resources/EmptyWithBorder.board").mkString
  var originalBoardPart = originalBoardString.split(models.Board.BOARD_GOALS_SEP)(0)
  var originalBoardPartLines = originalBoardPart.split(models.Board.END_OF_LINE)

  var originalGoalsPart = originalBoardString.split(models.Board.BOARD_GOALS_SEP)(1)
  var originalGoalsPartLines = originalGoalsPart.split(models.Board.END_OF_LINE)


  val board = models.Board.boardFromString(0, "empty", originalBoardString)
  var serialized = models.Board.boardToString(board)

}

class TestBoardSerialization extends Specification {
  "saving the empty (only border) board loaded from file" should {

    "keep the same number of lines for the specification of the board layout" in new EmptyBoardFromFile{

      var serializedBoardPart = serialized.split(models.Board.BOARD_GOALS_SEP)(0)
      var serializedLines = serializedBoardPart.split(models.Board.END_OF_LINE)

      serializedLines.length mustEqual originalBoardPartLines.length
    }

    "keep the same format for each of the lines (ignoring new line characters)" in new EmptyBoardFromFile{

      var serializedBoardPart = serialized.split(models.Board.BOARD_GOALS_SEP)(0)
      var serializedLines = serializedBoardPart.split(models.Board.END_OF_LINE)

      for(i <- 0 to serializedLines.length-1){
        serializedLines(i) mustEqual originalBoardPartLines(i)
      }
    }

    "keep the same number of goals as the original" in new EmptyBoardFromFile{

      var serializedGoalsPart = serialized.split(models.Board.BOARD_GOALS_SEP)(1)
      var serializedGoalsLines = serializedGoalsPart.split(models.Board.END_OF_LINE)

      serializedGoalsLines.length mustEqual originalGoalsPartLines.length
    }

    "keep the same goals for each of the lines" in new EmptyBoardFromFile{

      var serializedGoalsPart = serialized.split(models.Board.BOARD_GOALS_SEP)(1)
      var serializedGoalsLines = serializedGoalsPart.split(models.Board.END_OF_LINE)

      for(i <- 0 to serializedGoalsLines.length-1){
        serializedGoalsLines(i) mustEqual originalGoalsPartLines(i)
      }
    }

  }



}
