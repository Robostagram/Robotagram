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

  def setGoal(x:Int, y:Int, goal:Goal){
    applyChange(x,y, c=> c.withGoal(goal))
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
