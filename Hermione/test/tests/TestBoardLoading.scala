package tests

import org.specs2.mutable.Specification
import java.io.File
import models.Color
import models.Symbol

class TestBoardLoading extends Specification {
  "check empty board only borders" in {
    val board = models.Board.boardFromFile("app/resources/EmptyWithBorder.board")
    board.width must equalTo(17)
    board.height must equalTo(17)
    for (i <- 0 to 16) {
      board.getCell(i, 0).wallTop must beTrue
      board.getCell(i, 0).wallBottom must beFalse
      board.getCell(i, 16).wallBottom must beTrue
      board.getCell(i, 16).wallTop must beFalse
      board.getCell(0, i).wallLeft must beTrue
      board.getCell(0, i).wallRight must beFalse
      board.getCell(16, i).wallRight must beTrue
      board.getCell(16, i).wallLeft must beFalse
    }

    for (i <- 1 to 15) {
      for (j <- 1 to 15) {
        board.getCell(j, i).wallTop must beFalse
        board.getCell(j, i).wallBottom must beFalse
        board.getCell(j, i).wallLeft must beFalse
        board.getCell(j, i).wallRight must beFalse
      }
    }

//    5,5,Yellow,Sun
//    6,7,Blue,Moon
//    16,4,Green,Gear
//    9,14,Red,Planet
//    10,10,Blue,Star
    board.getCell(5,5).goal.color must be_==(Color.Yellow)
    board.getCell(5,5).goal.symbol must be_==(Symbol.Sun)
    board.getCell(7,6).goal.color must be_==(Color.Blue)
    board.getCell(7,6).goal.symbol must be_==(Symbol.Moon)
    board.getCell(4,16).goal.color must be_==(Color.Green)
    board.getCell(4,16).goal.symbol must be_==(Symbol.Gear)
    board.getCell(14,9).goal.color must be_==(Color.Red)
    board.getCell(14,9).goal.symbol must be_==(Symbol.Planet)
    board.getCell(10,10).goal.color must be_==(Color.Blue)
    board.getCell(10,10).goal.symbol must be_==(Symbol.Star)

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
