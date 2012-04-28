package models

class Board(val width: Int, val height: Int) {
  val cells: Array[Array[Cell]] =  Array.fill(width, height){ new Cell(false, false, null) }

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
               case "-" => cells(w)(h) = new Cell(true, false, null)
               case "|" => cells(w)(h) = new Cell(false, true, null)
               case "T" => cells(w)(h) = new Cell(true, true, null)
               case _ => ()
           }
         }
    }
    board
  }

}
