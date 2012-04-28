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
  def boardFromString(stringBoard: String): Board = {
    val lines = stringBoard.split("\\r[\\n]{0,1}")
    val h = lines.length
    val w = lines(0).length
    val board = new Board(w, h)
    for (line <- lines) {
      for (char <- line.split("")) {
        char match {
          case "a" => board.cells(w)(h) = new Cell(true, false, false, false, null)
          case "b" => board.cells(w)(h) = new Cell(false, true, false, false, null)
          case "c" => board.cells(w)(h) = new Cell(false, false, true, false, null)
          case "d" => board.cells(w)(h) = new Cell(false, false, false, true, null)
          case "e" => board.cells(w)(h) = new Cell(true, true, false, false, null)
          case "f" => board.cells(w)(h) = new Cell(true, false, true, false, null)
          case "g" => board.cells(w)(h) = new Cell(true, false, false, true, null)
          case "h" => board.cells(w)(h) = new Cell(false, true, true, false, null)
          case "i" => board.cells(w)(h) = new Cell(false, true, false, true, null)
          case "j" => board.cells(w)(h) = new Cell(false, false, true, true, null)
          case "k" => board.cells(w)(h) = new Cell(true, true, true, false, null)
          case "l" => board.cells(w)(h) = new Cell(true, true, false, true, null)
          case "m" => board.cells(w)(h) = new Cell(true, false, true, true, null)
          case "n" => board.cells(w)(h) = new Cell(false, true, true, true, null)
          case "o" => board.cells(w)(h) = new Cell(true, true, true, true, null)
          case _ => ()
        }
      }
    }
    board
  }
}
