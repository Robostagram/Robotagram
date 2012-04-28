package tests

import org.specs2.mutable.Specification
import java.io.File

class TestBoardLoading extends Specification {
  "check empty board only borders" in {
    val board = models.Board.boardFromFile("app/resources/EmptyWithBorder.board")
    board.width must equalTo(16)
    board.height must equalTo(16)
    for (i <- 0 to 15) {
      board.getCell(i, 0).wallTop must beTrue
      board.getCell(i, 0).wallBottom must beFalse
      board.getCell(i, 15).wallBottom must beTrue
      board.getCell(i, 15).wallTop must beFalse
      board.getCell(0, i).wallLeft must beTrue
      board.getCell(0, i).wallRight must beFalse
      board.getCell(15, i).wallRight must beTrue
      board.getCell(15, i).wallLeft must beFalse
    }

    for (i <- 1 to 14) {
      for (j <- 1 to 14) {
        board.getCell(j, i).wallTop must beFalse
        board.getCell(j, i).wallBottom must beFalse
        board.getCell(j, i).wallLeft must beFalse
        board.getCell(j, i).wallRight must beFalse
      }
    }
  }

  "check board with borders and square center" in {
    val board = models.Board.boardFromFile("app/resources/OneSquareWithBorder.board")
    board.width must equalTo(16)
    board.height must equalTo(16)
    for (i <- 0 to 15) {
      board.getCell(i, 0).wallTop must beTrue
      board.getCell(i, 0).wallBottom must beFalse
      board.getCell(i, 15).wallBottom must beTrue
      board.getCell(i, 15).wallTop must beFalse
      board.getCell(0, i).wallLeft must beTrue
      board.getCell(0, i).wallRight must beFalse
      board.getCell(15, i).wallRight must beTrue
      board.getCell(15, i).wallLeft must beFalse
    }

    //above square cells
    board.getCell(7, 6).wallTop must beFalse
    board.getCell(7, 6).wallRight must beFalse
    board.getCell(7, 6).wallBottom must beTrue
    board.getCell(7, 6).wallLeft must beFalse

    board.getCell(8, 6).wallTop must beFalse
    board.getCell(8, 6).wallRight must beFalse
    board.getCell(8, 6).wallBottom must beTrue
    board.getCell(8, 6).wallLeft must beFalse

    //right of square cells
    board.getCell(9, 7).wallTop must beFalse
    board.getCell(9, 7).wallRight must beFalse
    board.getCell(9, 7).wallBottom must beFalse
    board.getCell(9, 7).wallLeft must beTrue

    board.getCell(9, 8).wallTop must beFalse
    board.getCell(9, 8).wallRight must beFalse
    board.getCell(9, 8).wallBottom must beFalse
    board.getCell(9, 8).wallLeft must beTrue

    //below square cells
    board.getCell(7, 9).wallTop must beTrue
    board.getCell(7, 9).wallRight must beFalse
    board.getCell(7, 9).wallBottom must beFalse
    board.getCell(7, 9).wallLeft must beFalse

    board.getCell(8, 9).wallTop must beTrue
    board.getCell(8, 9).wallRight must beFalse
    board.getCell(8, 9).wallBottom must beFalse
    board.getCell(8, 9).wallLeft must beFalse

    //left of square cells
    board.getCell(6, 7).wallTop must beFalse
    board.getCell(6, 7).wallRight must beTrue
    board.getCell(6, 7).wallBottom must beFalse
    board.getCell(6, 7).wallLeft must beFalse

    board.getCell(6, 8).wallTop must beFalse
    board.getCell(6, 8).wallRight must beTrue
    board.getCell(6, 8).wallBottom must beFalse
    board.getCell(6, 8).wallLeft must beFalse

    //inside square cells
    board.getCell(7, 7).wallTop must beTrue
    board.getCell(7, 7).wallRight must beFalse
    board.getCell(7, 7).wallBottom must beFalse
    board.getCell(7, 7).wallLeft must beTrue

    board.getCell(8, 7).wallTop must beTrue
    board.getCell(8, 7).wallRight must beTrue
    board.getCell(8, 7).wallBottom must beFalse
    board.getCell(8, 7).wallLeft must beFalse

    board.getCell(7, 8).wallTop must beFalse
    board.getCell(7, 8).wallRight must beFalse
    board.getCell(7, 8).wallBottom must beTrue
    board.getCell(7, 8).wallLeft must beTrue

    board.getCell(8, 8).wallTop must beFalse
    board.getCell(8, 8).wallRight must beTrue
    board.getCell(8, 8).wallBottom must beTrue
    board.getCell(8, 8).wallLeft must beFalse
  }

}
