package models

class Board(val width: Int, val height: Int) {
  val cells: Array[Array[Cell]] =  Array.fill(height, width){ Cell.Empty }

  def setTop(x:Int, y:Int, wall:Boolean = true){
    cells(x)(y) = cells(x)(y).withTop(wall)
    if (y > 0) cells(x)(y-1) = cells(x)(y-1).withBottom(wall)
  }

  def setBottom(x:Int, y:Int, wall:Boolean = true){
    cells(x)(y) = cells(x)(y).withBottom(wall)
    if (y < height-1) cells(x)(y+1) = cells(x)(y+1).withTop(wall)
  }

  def setRight(x:Int, y:Int, wall:Boolean = true){
    cells(x)(y) = cells(x)(y).withRight(wall)
    if (x < width-1) cells(x+1)(y) = cells(x+1)(y).withLeft(wall)
  }

  def setLeft(x:Int, y:Int, wall:Boolean = true){
    cells(x)(y) = cells(x)(y).withLeft(wall)
    if (x > 0) cells(x-1)(y) = cells(x-1)(y).withRight(wall)
  }
}

object Board {
  def boardFromString(stringBoard: String): Board = {
    val lines = stringBoard.split("\\r[\\n]{0,1}")
    val h = lines.length
    val w = lines(0).length
    val board = new Board(w, h)
    for(line <- lines) {
         for(char <- line.split("")) {
           char match {
              // case "-" => cells(w)(h) = new Cell(true, false, null)
               //case "|" => cells(w)(h) = new Cell(false, true, null)
               //case "T" => cells(w)(h) = new Cell(true, true, null)
               case _ => ()
           }
         }
    }
    board
  }
}
