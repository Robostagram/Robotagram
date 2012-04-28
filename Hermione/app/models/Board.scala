package models

class Board(val width: Int, val height: Int) {
  val cells: Array[Array[Cell]] = Array.fill(height, width) {
    Cell.Empty
  }

  def withTop(x:Int, y:Int, wall:Boolean = true){
    cells(x)(y) = cells(x)(y).withTop(wall)
    if (y > 0) cells(x)(y - 1) = cells(x)(y - 1).withBottom(wall)
  }

  def withBottom(x:Int, y:Int, wall:Boolean = true){
    cells(x)(y) = cells(x)(y).withBottom(wall)
    if (y < height-1) cells(x)(y+1) = cells(x)(y+1).withTop(wall)
  }

  def withRight(x:Int, y:Int, wall:Boolean = true){
    // TODO
  }

  def withLeft(x:Int, y:Int, wall:Boolean = true){
    // TODO
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
