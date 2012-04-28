package tests

import org.specs2.mutable.Specification
import java.io.File

class TestBoardLoading extends Specification {
  "check empty board only borders" in {
    val board = models.Board.boardFromFile("app/resources/EmptyWithBorder.board")
    board.width must equalTo(16)
    board.height must equalTo(16)
    for (i <- 0 to 15) {
      board.getCell(i, 0).wallTop must equalTo(true)
      board.getCell(i, 0).wallBottom must equalTo(false)
      board.getCell(i, 15).wallBottom must equalTo(true)
      board.getCell(i, 15).wallTop must equalTo(false)
      board.getCell(0, i).wallLeft must equalTo(true)
      board.getCell(0, i).wallRight must equalTo(false)
      board.getCell(15, i).wallRight must equalTo(true)
      board.getCell(15, i).wallLeft must equalTo(false)
    }

    for (i <- 1 to 14) {
      for (j <- 1 to 14) {
        board.getCell(j, i).wallTop must equalTo(false)
        board.getCell(j, i).wallBottom must equalTo(false)
        board.getCell(j, i).wallLeft must equalTo(false)
        board.getCell(j, i).wallRight must equalTo(false)
      }
    }
  }

}
