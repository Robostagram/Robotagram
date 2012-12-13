package models

import scala._
import collection.mutable.ListBuffer
import collection.immutable.{HashSet, HashMap, Map}
import models.Color._
import scala.util.Random


class Board(val id : Long, val name : String, val width: Int, val height: Int) {

  private val cells: Array[Array[Cell]] = Array.fill(height, width) {
    EmptyCell
  }
  
  def setTop(col: Int, row: Int, wall: Boolean = true) {
    applyChange(col, row, c => c.withTop(wall))
    if (row > 0) applyChange(col, row - 1, c => c.withBottom(wall))
  }

  def setBottom(col: Int, row: Int, wall: Boolean = true) {
    applyChange(col, row, c => c.withBottom(wall))
    if (row < height - 1) applyChange(col, row + 1, c => c.withTop(wall))
  }

  def setRight(col: Int, row: Int, wall: Boolean = true) {
    applyChange(col, row, c => c.withRight(wall))
    if (col < width - 1) applyChange(col + 1, row, c => c.withLeft(wall))
  }

  def setLeft(col: Int, row: Int, wall: Boolean = true) {
    applyChange(col, row, c => c.withLeft(wall))
    if (col > 0) applyChange(col - 1, row, c => c.withRight(wall))
  }

  def setGoal(col: Int, row: Int, goal: Goal) {
    applyChange(col, row, c => c.withGoal(goal))
  }

  def getCell(col: Int, row: Int): Cell = {
    cells(row)(col)
  }

  def setCell(col: Int, row: Int, cell: Cell) {
    cells(row)(col) = cell
  }
  
  // look for a goal by attributs (color and symbol). returns its coordinates (col, row) or (-1, -1) if none found
  def findGoalPosition(goal: Goal): (Int, Int) = {
    if(goal != null) {
      for (row <- 0 until height) {
        for (col <- 0 until width) {
          val cGoal = cells(row)(col).goal
          if(cGoal != null) {
            if(cGoal.color == goal.color && cGoal.symbol == goal.symbol) {
              return (col, row)
            }
          }
        }
      }
    }
    (-1, -1)
  }

  private def applyChange(col: Int, row: Int, map: Cell => Cell) {
    setCell(col, row, map(getCell(col, row)))
  }

  private def debugDump() {
    for (row <- 0 until height) {
      for (col <- 0 until width) {
        val cell = cells(row)(col)
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
    val newBoard = new Board(id, name, height, width)
    for (row <- 0 until height) {
      for (col <- 0 until width) {
        newBoard.cells(col)(height - row - 1) = cells(row)(col).rotate90deg()
      }
    }
    newBoard
  }

  // decide how to transform the board
  def transformQuarters(quarterOrder: Tuple3[Int, Int, Int]) : Board= {
    if (width != height || width % 2 != 0) {
      //only square boards odd length can be shuffled by quarter
      this
    } else {
      val half = width / 2: Int

      // NE, SE and SW quarters in that order
      val quarters = Array.fill(4) {
        new Board(0, "zz", half, half)
      }
      //fill the quarters
      for (i <- 0 until half) {
        Array.copy(cells(i), 0, quarters(0).cells(i), 0, half)
        Array.copy(cells(i), half, quarters(1).cells(i), 0, half)
        Array.copy(cells(half + i), half, quarters(2).cells(i), 0, half)
        Array.copy(cells(half + i), 0, quarters(3).cells(i), 0, half)
      }

      //first quarter is always the same, the other only are switched/rotated around
      val rBoard = new Board(id, name, width, height)
      for (i <- 0 until half) {
        Array.copy(quarters(0).cells(i), 0, rBoard.cells(i), 0, half)
      }

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

  // get random robots positions for each color for a given board
  def randomRobots(board:Board): Map[Color, Robot] = {
    var robots = new HashMap[Color, Robot]()
    var setPos:Set[Tuple2[Int,Int]] = new HashSet[Tuple2[Int,Int]];
    for(c <- Color.values){
      var col:Int = Random.nextInt(board.width)
      var row:Int = Random.nextInt(board.height)
      while(setPos.contains((col,row)) || unacceptableValue(board, col, row)){
        col = Random.nextInt(board.width)
        row = Random.nextInt(board.height)
      }
      setPos+=((col,row))
      robots += ((c, new Robot(c,col,row)))
    }
    robots
  }


  // detects middle (closed) square of the board, and goals
  def unacceptableValue(board:Board, col:Int, row:Int):Boolean = {
    if(board.cells(row)(col).goal != null) {
      return true
    }
    val h = board.height
    val w = board.width

    var resultW = true
    var resultH = true
    var midH = h/2;
    var midW = w/2;
    if(w % 2 ==0){
      resultW = col==midW||col==midW-1
    } else{
      resultW = col==(w-1)/2
    }
    if(h % 2 ==0){
      resultH = row==midH||row==midH-1
    } else{
      resultH = row==(h-1)/2
    }
    resultW && resultH
  }


  // these are REGEX !!
  val END_OF_LINE = "[\\r]{0,1}\\n"
  val BOARD_GOALS_SEP = END_OF_LINE + "\\#" + END_OF_LINE
  val GOAL_SEP = ","

  def boardFromFile(path: String, id:Long, name:String): Board = {
    boardFromString(id, name, scala.io.Source.fromFile(path).mkString)
  }

  def boardFromString(id:Long, name:String, rawFile: String): Board = {
    val rawSplit = rawFile.split(BOARD_GOALS_SEP)
    val stringBoard = rawSplit(0)
    val goals = rawSplit(1)
    val lines = stringBoard.split(END_OF_LINE)
    val h = lines.length
    val w = lines(0).length
    val board = new Board(id, name, w, h)
    var row = 0
    for (line <- lines) {
      var col = 0
      for (char <- line.toCharArray) {
        char match {
          case 'a' => board.cells(row)(col) = updateCell(board, row, col, true, false, false, false)
          case 'b' => board.cells(row)(col) = updateCell(board, row, col, false, true, false, false)
          case 'c' => board.cells(row)(col) = updateCell(board, row, col, false, false, true, false)
          case 'd' => board.cells(row)(col) = updateCell(board, row, col, false, false, false, true)
          case 'e' => board.cells(row)(col) = updateCell(board, row, col, true, true, false, false)
          case 'f' => board.cells(row)(col) = updateCell(board, row, col, true, false, true, false)
          case 'g' => board.cells(row)(col) = updateCell(board, row, col, true, false, false, true)
          case 'h' => board.cells(row)(col) = updateCell(board, row, col, false, true, true, false)
          case 'i' => board.cells(row)(col) = updateCell(board, row, col, false, true, false, true)
          case 'j' => board.cells(row)(col) = updateCell(board, row, col, false, false, true, true)
          case 'k' => board.cells(row)(col) = updateCell(board, row, col, true, true, true, false)
          case 'l' => board.cells(row)(col) = updateCell(board, row, col, true, true, false, true)
          case 'm' => board.cells(row)(col) = updateCell(board, row, col, true, false, true, true)
          case 'n' => board.cells(row)(col) = updateCell(board, row, col, false, true, true, true)
          case 'o' => board.cells(row)(col) = updateCell(board, row, col, true, true, true, true)
          case _ => ()
        }
        col += 1
      }
      row += 1
    }
    for (line <- goals.split(END_OF_LINE)) {
      val elements = line.split(GOAL_SEP)
      val row = Integer.parseInt(elements(0))
      val col = Integer.parseInt(elements(1))
      val oldCell = board.cells(row)(col)
      board.cells(row)(col) = oldCell.withGoal(new Goal(Color.withName(elements(2)), Symbol.withName(elements(3))))
    }
    board
  }

  def updateCell(board: Board, row: Int, col: Int, top: Boolean, right: Boolean, bottom: Boolean, left: Boolean): Cell = {
    val cell = new Cell(top, right, bottom, left, null)
    updateUpAndLeft(board, cell, row, col)
    cell
  }

  def updateUpAndLeft(board: Board, newCell: Cell, row: Int, col: Int) {
    if (row > 0) {
      board.cells(row - 1)(col) = board.cells(row - 1)(col).withBottom(newCell.wallTop)
    }
    if (col > 0) {
      board.cells(row)(col - 1) = board.cells(row)(col - 1).withRight(newCell.wallLeft)
    }
  }

  // these are just strings appended in the output
  val SERIALIZE_END_OF_LINE = "\r\n" // TODO : should probably be the system value of new line ?
  val SERIALIZE_BOARD_GOALS_SEP = "#" + SERIALIZE_END_OF_LINE
  def boardToString(board: Board): String = {
    val result = new StringBuilder()
    val goals = new ListBuffer[Tuple4[Int,Int,Color.Color ,Symbol.Symbol]]()
    for (rowId <- 0 to board.cells.length -1){
      val row = board.cells(rowId)

      for (colId <- 0 to board.cells(rowId).length-1){
        val cell = row(colId)
        val walls = (cell.wallTop, cell.wallRight, cell.wallBottom, cell.wallLeft)
        result.append(
          walls match{
            case (true, false, false, false) => 'a'
            case (false, true, false, false) => 'b'
            case (false, false, true, false) => 'c'
            case (false, false, false, true) => 'd'
            case (true, true, false, false)  => 'e'
            case (true, false, true, false)  => 'f'
            case (true, false, false, true)  => 'g'
            case (false, true, true, false)  => 'h'
            case (false, true, false, true)  => 'i'
            case (false, false, true, true)  => 'j'
            case (true, true, true, false)   => 'k'
            case (true, true, false, true)   => 'l'
            case (true, false, true, true)   => 'm'
            case (false, true, true, true)   => 'n'
            case (true, true, true, true)    => 'o'
            case (false, false, false, false)=> ' ' // do not forget "no walls"
          }
        )

        if (cell.goal != null){
          goals.append((rowId, colId, cell.goal.color, cell.goal.symbol))
        }

      }
      result.append(SERIALIZE_END_OF_LINE)

    }
    result.append(SERIALIZE_BOARD_GOALS_SEP)

    // serialize the goals
    goals.foreach { goal =>
      goal match{
        case (col, row, color, symb) => {
          result.append(col + GOAL_SEP + row + GOAL_SEP + color.toString + GOAL_SEP + symb.toString)
          result.append(SERIALIZE_END_OF_LINE)
        }
      }
    }

    result.toString()
  }

  def loadById(id: Long): Option[Board] = {
    DbBoard.findById(id).map{ dbBoard =>
      boardFromString(dbBoard.id.get, dbBoard.name, dbBoard.data)
    }
  }


}
