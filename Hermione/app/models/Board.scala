package models

class Board(val width: Int, val height: Int) {
  val cells: Array[Array[Cell]] =  Array.fill(height, width){ Cell.Empty }
}
