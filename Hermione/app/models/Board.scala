package models

import play.db.ebean.Model

class Board(w: Int, h: Int) extends Model{
  val width: Int = w
  val height: Int = h

  val cells: Array[Array[Cell]] =  Array.fill(height, width){ new Cell(false, false, null) }
}
