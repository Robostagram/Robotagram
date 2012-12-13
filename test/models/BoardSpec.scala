package models

import org.specs2.mutable._
import org.specs2.specification.Scope


trait EmptyBoard extends Scope {
  val height = 10
  val width = 5
  val board = new Board(0, "testBoard", width, height)
}

trait EmptySquareBoard extends Scope {
  val width = 8
  val board = new Board(1, "testSquareBoard", width, width)
}

trait EmptySquareBoardWithGoal extends EmptySquareBoard {
  val goalR1 = new Goal(Color.Red, Symbol.ONE)
  val goalB2 = new Goal(Color.Blue, Symbol.TWO)
  val goalG3 = new Goal(Color.Green, Symbol.THREE)
  val goalY4 = new Goal(Color.Yellow, Symbol.FOUR)
  board.setGoal(2, 1, goalR1)
  board.setGoal(5, 0, goalB2)
  board.setGoal(4, 5, goalG3)
  board.setGoal(1, 6, goalY4)
} 

class EmptyBoardSpec extends Specification {
  "An empty board of size n * m" should {
    "have n lines consisting of m empty cells" in new EmptyBoard {
      for (col <- 0 until width) {
        for (row <- 0 until height) {
          board.getCell(col, row) must beEqualTo(EmptyCell)
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
  
  // findGoalPosition
  
  "Calling findGoalPosition(someGoal)" should {
    val xR1 = 4
    val yR1 = 2
    val xB2 = 1
    val yB2 = 3
    val goalR1 = new Goal(Color.Red, Symbol.ONE)
    val goalB2 = new Goal(Color.Blue, Symbol.TWO)
    "find (-1, -1) in EmptyBoard" in new EmptyBoard {
      board.findGoalPosition(goalR1) must be_==((-1,-1))
      board.findGoalPosition(goalB2) must be_==((-1,-1))
    }
    "find (x,y) in board with goal set at (x,y)" in new EmptyBoard {
      board.setGoal(xR1, yR1, goalR1)
      board.findGoalPosition(goalR1) must be_==((xR1,yR1))
      board.findGoalPosition(goalB2) must be_==((-1,-1))
      board.setGoal(xB2, yB2, goalB2)
      board.findGoalPosition(goalB2) must be_==((xB2,yB2))
    }
  }
  
  // rotate90Deg
  
  "Calling rotate90Deg" should {
    "produce a rotated board" in new EmptyBoard {
      val xR1 = 4
      val yR1 = 2
      val xB2 = 1
      val yB2 = 3
      val goalR1 = new Goal(Color.Red, Symbol.ONE)
      val goalB2 = new Goal(Color.Blue, Symbol.TWO)
      board.setGoal(xR1, yR1, goalR1)
      board.setGoal(xB2, yB2, goalB2)
      val rotated = board.rotate90deg()
      rotated.height must be_==(board.width)
      rotated.width must be_==(board.height)
      rotated.id must be_==(board.id)
      rotated.name must be_==(board.name)
      rotated.findGoalPosition(goalR1) must be_==((7,4))
      rotated.findGoalPosition(goalB2) must be_==((6,1))
    }
  }
  
  // transformQuarters
  
  "Calling transformQuarters(_,_,_) of a non square board" should {
    "produce the same board" in new EmptyBoard {
      board.transformQuarters((3,2,1)) must be_==(board)
    }
  }
  
  "Calling transformQuarters(x,y,z) of a square board" should {
    "produce an equal board when x,y,z = 1,2,3" in new EmptySquareBoardWithGoal {
      val shuffledBoard = board.transformQuarters((1,2,3))
      shuffledBoard.findGoalPosition(goalR1) must be_==((2,1))
      shuffledBoard.findGoalPosition(goalB2) must be_==((5,0))
      shuffledBoard.findGoalPosition(goalG3) must be_==((4,5))
      shuffledBoard.findGoalPosition(goalY4) must be_==((1,6))
    }
    
    "produce a board with NE and SW mirrored when x,y,z = 3,2,1" in new EmptySquareBoardWithGoal {
      val shuffledBoard = board.transformQuarters((3,2,1))
      shuffledBoard.findGoalPosition(goalR1) must be_==((2,1))
      shuffledBoard.findGoalPosition(goalB2) must be_==((2,7))
      shuffledBoard.findGoalPosition(goalG3) must be_==((4,5))
      shuffledBoard.findGoalPosition(goalY4) must be_==((6,1))
    }
  }
  
  // unacceptable values
  
  "unacceptableValue" should {
    "not accept cells with a goal" in new EmptySquareBoardWithGoal {
      Board.unacceptableValue(board, 2, 1) must beTrue
      Board.unacceptableValue(board, 5, 0) must beTrue
      Board.unacceptableValue(board, 4, 5) must beTrue
      Board.unacceptableValue(board, 1, 6) must beTrue
    }
    "not accept cells in the middle zone" in new EmptySquareBoardWithGoal {
      Board.unacceptableValue(board, 3, 3) must beTrue
      Board.unacceptableValue(board, 3, 4) must beTrue
      Board.unacceptableValue(board, 4, 3) must beTrue
      Board.unacceptableValue(board, 4, 4) must beTrue
    }
    "accept all empty cells not in the middle zone" in new EmptyBoard {
      for {
       row <- 1 until height
       col <- 0 until width
       if col != 2 || (row != 4 && row != 5)
      } Board.unacceptableValue(board, col, row) must beFalse
    } 
    
  }
  
}
