package models

import util.Random
import scala.{Int, Array}


class Board(val width: Int, val height: Int) {

  val QUARTER_COMBOS = Array((1, 2, 3), (1, 3, 2), (2, 1, 3), (2, 3, 1), (3, 1, 2), (3, 2, 1))

  val cells: Array[Array[Cell]] = Array.fill(height, width) {
    EmptyCell
  }

  def setTop(x: Int, y: Int, wall: Boolean = true) {
    applyChange(x, y, c => c.withTop(wall))
    if (y > 0) applyChange(x, y - 1, c => c.withBottom(wall))
  }

  def setBottom(x: Int, y: Int, wall: Boolean = true) {
    applyChange(x, y, c => c.withBottom(wall))
    if (y < height - 1) applyChange(x, y + 1, c => c.withTop(wall))
  }

  def setRight(x: Int, y: Int, wall: Boolean = true) {
    applyChange(x, y, c => c.withRight(wall))
    if (x < width - 1) applyChange(x + 1, y, c => c.withLeft(wall))
  }

  def setLeft(x: Int, y: Int, wall: Boolean = true) {
    applyChange(x, y, c => c.withLeft(wall))
    if (x > 0) applyChange(x - 1, y, c => c.withRight(wall))
  }

  def setGoal(x: Int, y: Int, goal: Goal) {
    applyChange(x, y, c => c.withGoal(goal))
  }

  // pour pouvoir faire board(1,2)
  def getCell(x: Int, y: Int): Cell = {
    cells(y)(x)
  }

  def setCell(x: Int, y: Int, cell: Cell) {
    cells(y)(x) = cell
  }
  
  // look for a goal by attributs (color and symbol). returns its coordinates or (-1, -1) if none found
  def findGoalPosition(goal: Goal): (Int, Int) = {
    if(goal != null) {
      for (i <- 0 until width) {
        for (j <- 0 until height) {
	      val cGoal = cells(i)(j).goal
	      if(cGoal != null) {
		    if(cGoal.color == goal.color && cGoal.symbol == goal.symbol) {
		      return (i, j)
		    }
	  	  }
	    }
	  }
	}
	(-1, -1)
  }

  private def applyChange(x: Int, y: Int, map: Cell => Cell) {
    setCell(x, y, map(getCell(x, y)))
  }

  private def debugDump() {
    for (i <- 0 until width) {
      for (j <- 0 until height) {
        val cell = cells(i)(j)
        print(if (cell.wallTop) "1" else "0")
        print(if (cell.wallRight) "1" else "0")
        print(if (cell.wallBottom) "1" else "0")
        print(if (cell.wallLeft) "1" else "0")
        print(" ")
      }
      println
    }
  }

  def rotate90deg(): Board = {
    val newBoard = new Board(height, width)
    for (i <- 0 until width) {
      for (j <- 0 until height) {
        newBoard.cells(j)(width - i - 1) = cells(i)(j).rotate90deg()
      }
    }
    newBoard
  }

  def randomizeQuarters(): Board = {
    if (width != height || width % 2 != 0) {
      //only square boards odd length can be shuffled by quarter
      this
    } else {
      val half = width / 2: Int

      // NE, SE and SW quarters in that order
      val quarters = Array.fill(4) {
        new Board(half, half)
      }
      //fill the quarters
      for (i <- 0 until half) {
        Array.copy(cells(i), 0, quarters(0).cells(i), 0, half)
        Array.copy(cells(i), half, quarters(1).cells(i), 0, half)
        Array.copy(cells(half + i), half, quarters(2).cells(i), 0, half)
        Array.copy(cells(half + i), 0, quarters(3).cells(i), 0, half)
      }

      //first quarter is always the same, the other only are switched/rotated around
      val rBoard = new Board(width, height)
      for (i <- 0 until half) {
        Array.copy(quarters(0).cells(i), 0, rBoard.cells(i), 0, half)
      }

      //reorder randomly the quarters
      val quarterOrder = QUARTER_COMBOS(new Random().nextInt(6))

      //North East quarter
      //diff gives the number of rotation to operate on the shuffled quarter
      var diff = 1 - quarterOrder._1
      diff = if (diff > 0) diff else diff + 4
      for (i <- 0 until diff) {
        quarters(quarterOrder._1) = quarters(quarterOrder._1).rotate90deg()
      }
      for (i <- 0 until half) {
        Array.copy(quarters(quarterOrder._1).cells(i), 0, rBoard.cells(i), half, half)
      }

      //South East quarter
      diff = 2 - quarterOrder._2
      diff = if (diff > 0) diff else diff + 4
      for (i <- 0 until diff) {
        quarters(quarterOrder._2) = quarters(quarterOrder._2).rotate90deg()
      }
      for (i <- 0 until half) {
        Array.copy(quarters(quarterOrder._2).cells(i), 0, rBoard.cells(i + half), half, half)
      }

      //South West quarter
      diff = 3 - quarterOrder._3
      diff = if (diff > 0) diff else diff + 4
      for (i <- 0 until diff) {
        quarters(quarterOrder._3) = quarters(quarterOrder._3).rotate90deg()
      }
      for (i <- 0 until half) {
        Array.copy(quarters(quarterOrder._3).cells(i), 0, rBoard.cells(i + half), 0, half)
      }

      //rebuild the seams
      for(i <- 0 until width) {
        var cell1 = rBoard.cells(i)(half-1)
        var cell2 = rBoard.cells(i)(half)
        var wall = cell1.wallRight || cell2.wallLeft
        rBoard.cells(i)(half-1) = cell1.withRight(wall)
        rBoard.cells(i)(half) = cell2.withLeft(wall)

        cell1 = rBoard.cells(half-1)(i)
        cell2 = rBoard.cells(half)(i)
        wall = cell1.wallBottom || cell2.wallTop
        rBoard.cells(half-1)(i) = cell1.withBottom(wall)
        rBoard.cells(half)(i) = cell2.withTop(wall)
      }

      rBoard
    }
  }
}

object Board {

  val END_OF_LINE = "[\\r]{0,1}\\n"
  val BOARD_GOALS_SEP = END_OF_LINE + "\\#" + END_OF_LINE
  val GOAL_SEP = ","

  def boardFromFile(path: String): Board = {
    boardFromString(scala.io.Source.fromFile(path).mkString)
  }

  def boardFromString(rawFile: String): Board = {
    val rawSplit = rawFile.split(BOARD_GOALS_SEP)
    val stringBoard = rawSplit(0)
    val goals = rawSplit(1)
    val lines = stringBoard.split(END_OF_LINE)
    val h = lines.length
    val w = lines(0).length
    val board = new Board(w, h)
    var i = 0
    for (line <- lines) {
      var j = 0
      for (char <- line.toCharArray) {
        char match {
          case 'a' => board.cells(i)(j) = updateCell(board, i, j, true, false, false, false)
          case 'b' => board.cells(i)(j) = updateCell(board, i, j, false, true, false, false)
          case 'c' => board.cells(i)(j) = updateCell(board, i, j, false, false, true, false)
          case 'd' => board.cells(i)(j) = updateCell(board, i, j, false, false, false, true)
          case 'e' => board.cells(i)(j) = updateCell(board, i, j, true, true, false, false)
          case 'f' => board.cells(i)(j) = updateCell(board, i, j, true, false, true, false)
          case 'g' => board.cells(i)(j) = updateCell(board, i, j, true, false, false, true)
          case 'h' => board.cells(i)(j) = updateCell(board, i, j, false, true, true, false)
          case 'i' => board.cells(i)(j) = updateCell(board, i, j, false, true, false, true)
          case 'j' => board.cells(i)(j) = updateCell(board, i, j, false, false, true, true)
          case 'k' => board.cells(i)(j) = updateCell(board, i, j, true, true, true, false)
          case 'l' => board.cells(i)(j) = updateCell(board, i, j, true, true, false, true)
          case 'm' => board.cells(i)(j) = updateCell(board, i, j, true, false, true, true)
          case 'n' => board.cells(i)(j) = updateCell(board, i, j, false, true, true, true)
          case 'o' => board.cells(i)(j) = updateCell(board, i, j, true, true, true, true)
          case _ => ()
        }
        j += 1
      }
      i += 1
    }
    for (line <- goals.split(END_OF_LINE)) {
      val elements = line.split(GOAL_SEP)
      val x = Integer.parseInt(elements(0))
      val y = Integer.parseInt(elements(1))
      val oldCell = board.cells(x)(y)
      board.cells(x)(y) = oldCell.withGoal(new Goal(Color.withName(elements(2)), Symbol.withName(elements(3))))
    }
    board
  }

  def updateCell(board: Board, i: Int, j: Int, top: Boolean, right: Boolean, bottom: Boolean, left: Boolean): Cell = {
    val cell = new Cell(top, right, bottom, left, null)
    updateUpAndLeft(board, cell, i, j)
    cell
  }

  def updateUpAndLeft(board: Board, newCell: Cell, i: Int, j: Int) {
    if (i > 0) {
      board.cells(i - 1)(j) = board.cells(i - 1)(j).withBottom(newCell.wallTop)
    }
    if (j > 0) {
      board.cells(i)(j - 1) = board.cells(i)(j - 1).withRight(newCell.wallLeft)
    }
  }
}
