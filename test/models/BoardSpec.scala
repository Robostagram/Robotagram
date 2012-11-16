package models

import org.specs2.mutable._
import org.specs2.specification.Scope


trait EmptyBoard extends Scope {
  val height = 6
  val width = 7
  val board = new Board(0, "theName", width, height)

}

class EmptyBoardSpec extends Specification {
  "An empty board of size n * m" should {
    "have n lines consisting of m empty cells" in new EmptyBoard {
      board.height must be_==(height)
      board.width must be_==(width)
      board.cells.length must be_==(height)
      board.cells(0).length must be_==(width)
      for (row <- board.cells) {
        for (cell <- row) {
          cell must beEqualTo(EmptyCell)
        }
      }
    }
  }



  // en haut à gauche

  "Calling setTop(0,0)" should {
    "set wall on top in (0,0)" in new EmptyBoard {
      board.setTop(0, 0)
      val cell = board.getCell(0, 0)
      cell.wallTop must beTrue
      cell.wallBottom must beFalse
      cell.wallLeft must beFalse
      cell.wallRight must beFalse
    }
  }

  "Calling setLeft(0,0)" should {
    "set wall on left in (0,0)" in new EmptyBoard {
      board.setLeft(0, 0)
      val cell = board.getCell(0, 0)
      cell.wallTop must beFalse
      cell.wallBottom must beFalse
      cell.wallLeft must beTrue
      cell.wallRight must beFalse
    }
  }

  "Calling setBottom(0,0)" should {
    "set wall on top in (0,1)" in new EmptyBoard {
      board.setBottom(0, 0)
      val cell = board.getCell(0, 1)
      cell.wallTop must beTrue
      cell.wallBottom must beFalse
      cell.wallLeft must beFalse
      cell.wallRight must beFalse
    }
  }

  "Calling setRight(0,0)" should {
    "set wall on left in (1,0)" in new EmptyBoard {
      board.setRight(0, 0)
      val cell = board.getCell(1, 0)
      cell.wallTop must beFalse
      cell.wallBottom must beFalse
      cell.wallLeft must beTrue
      cell.wallRight must beFalse
    }
  }



  // en haut à droite

  "Calling setTop(width -1, 0)" should {
    "set wall on top in (width -1 ,0)" in new EmptyBoard {
      board.setTop(width - 1, 0)
      val cell = board.getCell(width - 1, 0)
      cell.wallTop must beTrue
      cell.wallBottom must beFalse
      cell.wallLeft must beFalse
      cell.wallRight must beFalse
    }
  }

  "Calling setRight(width -1, 0)" should {
    "set wall on right in (width -1 ,0)" in new EmptyBoard {
      board.setRight(width - 1, 0)
      val cell = board.getCell(width - 1, 0)
      cell.wallTop must beFalse
      cell.wallBottom must beFalse
      cell.wallLeft must beFalse
      cell.wallRight must beTrue
    }
  }

  "Calling setBottom(width -1,0)" should {
    "set wall on top in (width -1, 1)" in new EmptyBoard {
      board.setBottom(width - 1, 0)
      val cell = board.getCell(width - 1, 1)
      cell.wallTop must beTrue
      cell.wallBottom must beFalse
      cell.wallLeft must beFalse
      cell.wallRight must beFalse
    }
  }

  "Calling setLeft(width -1,0)" should {
    "set wall on right in (width -2, 0)" in new EmptyBoard {
      board.setLeft(width - 1, 0)
      val cell = board.getCell(width - 2, 0)
      cell.wallTop must beFalse
      cell.wallBottom must beFalse
      cell.wallLeft must beFalse
      cell.wallRight must beTrue
    }
  }

  // en bas à gauche

  "Calling setBottom(0,height-1)" should {
    "set wall on bottom in (0,height-1)" in new EmptyBoard {
      board.setBottom(0, height - 1)
      val cell = board.getCell(0, height - 1)
      cell.wallTop must beFalse
      cell.wallBottom must beTrue
      cell.wallLeft must beFalse
      cell.wallRight must beFalse
    }
  }

  "Calling setLeft(0,height-1)" should {
    "set wall on left in (0,height-1)" in new EmptyBoard {
      board.setLeft(0, height - 1)
      val cell = board.getCell(0, height - 1)
      cell.wallTop must beFalse
      cell.wallBottom must beFalse
      cell.wallLeft must beTrue
      cell.wallRight must beFalse
    }
  }


  "Calling setRight(0,height-1)" should {
    "set wall on left in (1,height-1)" in new EmptyBoard {
      board.setRight(0, height - 1)
      val cell = board.getCell(1, height - 1)
      cell.wallTop must beFalse
      cell.wallBottom must beFalse
      cell.wallLeft must beTrue
      cell.wallRight must beFalse
    }
  }

  "Calling setTop(0,height-1)" should {
    "set wall on bottom in (0,height-2)" in new EmptyBoard {
      board.setTop(0, height - 1)
      val cell = board.getCell(0, height - 2)
      cell.wallTop must beFalse
      cell.wallBottom must beTrue
      cell.wallLeft must beFalse
      cell.wallRight must beFalse
    }
  }



  // en bas à droite

  "Calling setBottom(width-1,height-1)" should {
    "set wall on bottom in (width-1,height-1)" in new EmptyBoard {
      board.setBottom(width - 1, height - 1)
      val cell = board.getCell(width - 1, height - 1)
      cell.wallTop must beFalse
      cell.wallBottom must beTrue
      cell.wallLeft must beFalse
      cell.wallRight must beFalse
    }
  }

  "Calling setRight(width-1,height-1)" should {
    "set wall on right in (width-1,height-1)" in new EmptyBoard {
      board.setRight(width - 1, height - 1)
      val cell = board.getCell(width - 1, height - 1)
      cell.wallTop must beFalse
      cell.wallBottom must beFalse
      cell.wallLeft must beFalse
      cell.wallRight must beTrue
    }
  }


  "Calling setTop(width-1,height-1)" should {
    "set wall on bottom in (width-1,height-2)" in new EmptyBoard {
      board.setTop(width - 1, height - 1)
      val cell = board.getCell(width - 1, height - 2)
      cell.wallTop must beFalse
      cell.wallBottom must beTrue
      cell.wallLeft must beFalse
      cell.wallRight must beFalse
    }
  }


  "Calling setLeft(width -1,height-1)" should {
    "set wall on right in (width -2, height-1)" in new EmptyBoard {
      board.setLeft(width - 1, height - 1)
      val cell = board.getCell(width - 2, height - 1)
      cell.wallTop must beFalse
      cell.wallBottom must beFalse
      cell.wallLeft must beFalse
      cell.wallRight must beTrue
    }
  }



  // SetGoal

  "Calling setGoal(x, y)" should {
    "put a goal at (x, y)" in new EmptyBoard {
      val x = 3
      val y = 4
      val goal = new Goal(Color.Red, Symbol.ONE)
      board.setGoal(x, y, goal)
      board.getCell(x, y).goal must be_==(goal)
    }
  }

}
