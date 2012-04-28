package models


class Board(val width: Int, val height: Int) {
  val cells: Array[Array[Cell]] = Array.fill(height, width) {
    EmptyCell
  }

  def setTop(x:Int, y:Int, wall:Boolean = true){
    applyChange(x,y,c=> c.withTop(wall))
    if (y > 0) applyChange(x,y-1,c=> c.withBottom(wall))
  }

  def setBottom(x:Int, y:Int, wall:Boolean = true){
    applyChange(x,y,c=> c.withBottom(wall))
    if (y < height-1) applyChange(x,y+1,c=> c.withTop(wall))
  }

  def setRight(x:Int, y:Int, wall:Boolean = true){
    applyChange(x,y,c=> c.withRight(wall))
    if (x < width-1) applyChange(x+1,y,c=> c.withLeft(wall))
  }

  def setLeft(x:Int, y:Int, wall:Boolean = true){
    applyChange(x,y,c=> c.withLeft(wall))
    if (x > 0) applyChange(x-1,y,c=> c.withRight(wall))
  }

  // pour pouvoir faire board(1,2)
  def getCell(x:Int, y:Int) : Cell ={
    cells(y)(x)
  }

  def setCell(x:Int, y:Int, cell: Cell){
    cells(y)(x) = cell
  }

  private def applyChange(x: Int, y:Int, map: Cell => Cell){
    setCell(x, y, map( getCell(x, y)))
  }
}

object Board {

  def boardFromFile(path: String): Board = {
    boardFromString(scala.io.Source.fromFile("file.txt").mkString)
  }

  def boardFromString(stringBoard: String): Board = {
    val lines = stringBoard.split("\\r[\\n]{0,1}")
    val h = lines.length
    val w = lines(0).length
    val board = new Board(w, h)
    var i = 0
    var j = 0
    for (line <- lines) {
      for (char <- line.split("")) {
        char match {
          case "a" => board.cells(i)(j) = updateCell(board, i, j, true, false, false, false)
          case "b" => board.cells(i)(j) = updateCell(board, i, j, false, true, false, false)
          case "c" => board.cells(i)(j) = updateCell(board, i, j, false, false, true, false)
          case "d" => board.cells(i)(j) = updateCell(board, i, j, false, false, false, true)
          case "e" => board.cells(i)(j) = updateCell(board, i, j, true, true, false, false)
          case "f" => board.cells(i)(j) = updateCell(board, i, j, true, false, true, false)
          case "g" => board.cells(i)(j) = updateCell(board, i, j, true, false, false, true)
          case "h" => board.cells(i)(j) = updateCell(board, i, j, false, true, true, false)
          case "i" => board.cells(i)(j) = updateCell(board, i, j, false, true, false, true)
          case "j" => board.cells(i)(j) = updateCell(board, i, j, false, false, true, true)
          case "k" => board.cells(i)(j) = updateCell(board, i, j, true, true, true, false)
          case "l" => board.cells(i)(j) = updateCell(board, i, j, true, true, false, true)
          case "m" => board.cells(i)(j) = updateCell(board, i, j, true, false, true, true)
          case "n" => board.cells(i)(j) = updateCell(board, i, j, false, true, true, true)
          case "o" => board.cells(i)(j) = updateCell(board, i, j, true, true, true, true)
          case _ => ()
        }
        j += 1
      }
      i += 1
    }
    board
  }

  def updateCell(board: Board, i: Int, j: Int, top: Boolean, right: Boolean, bottom: Boolean, left: Boolean): Cell = {
    val cell = new Cell(top, right, bottom, left, null)
    updateUpAndLeft(board, i, j)
    cell
  }

  def updateUpAndLeft(board: Board, i: Int, j: Int) {
    val newCell = board.cells(i)(j)
    if (i > 0) {
      board.cells(i-1)(j) = board.cells(i-1)(j).withBottom()
    }
    if(j > 0) {
      board.cells(i)(j-1) = board.cells(i)(j-1).withRight()
    }
  }
}
