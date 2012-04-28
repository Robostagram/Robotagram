package models

class Cell(wallNorth:Boolean,wallEast:Boolean,goal:Goal) {

  def withEastWall(yesOrNo : Boolean = true): Cell ={
    new Cell(wallNorth, yesOrNo, goal)
  }

  def withNorthWall(yesOrNo : Boolean = true): Cell ={
    new Cell(yesOrNo, wallEast, goal)
  }

  def withGoal(g : Goal): Cell ={
    new Cell(wallNorth, wallEast, g)
  }

  def wallTop:Boolean = wallNorth;
  def wallRight:Boolean = wallEast;
  def wallLeft:Boolean = wallNorth;  // TODO : compute it !
  def wallBottom:Boolean = wallNorth; // TODO : compute it !

}

object Cell{
  def Empty = {new Cell(false, false, null)}
}

