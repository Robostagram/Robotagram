package model

class Cell(north:Boolean,east:Boolean,g:Goal) {
  val wallTop:Boolean = north;
  val wallRight:Boolean = east;
  val wallLeft:Boolean = east;
  val wallBottom:Boolean = east;
  val goal:Goal = g;
}
