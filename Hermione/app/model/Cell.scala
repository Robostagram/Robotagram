package model

class Cell(north:Boolean,east:Boolean,g:Goal) {
  val wallNorth:Boolean = north;
  val wallEast:Boolean = east;
  val goal:Goal = g;


  def withEastWall(yesOrNo : Boolean = true): Cell ={
    new Cell(wallNorth, yesOrNo, goal)
  }

  def withNorthWall(yesOrNo : Boolean = true): Cell ={
    new Cell(yesOrNo, wallEast, goal)
  }

  def withGoal(g : Goal): Cell ={
    new Cell(wallNorth, wallEast, g)
  }
}


object Cell{
  def Empty : Cell = {
    new Cell(false, false, null);
  }
}

