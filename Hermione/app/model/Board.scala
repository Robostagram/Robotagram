package model

class Board(w: Int, h: Int) {
  val width: Int = w
  val height: Int = h

  val cells: Array[Array[Cell]] =  Array.fill(height, width){ new Cell() }
}
