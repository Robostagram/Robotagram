package model

class Cell(north:Boolean,east:Boolean,g:Goal) {
  val wallNorth:Boolean = north;
  val wallEast:Boolean = east;
  val goal:Goal = g;
}
