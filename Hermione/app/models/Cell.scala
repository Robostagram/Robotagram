package models

class Cell(wallTop:Boolean,wallRight:Boolean, wallBottom:Boolean, wallLeft:Boolean,goal:Goal) {

  def withRight(yesOrNo : Boolean = true): Cell ={
    new Cell(wallTop,  yesOrNo, wallBottom, wallLeft, goal)
  }

  def withTop(yesOrNo : Boolean = true): Cell ={
    new Cell(yesOrNo,  wallRight, wallBottom, wallLeft, goal)
  }

  def withLeft(yesOrNo : Boolean = true): Cell ={
    new Cell(wallTop,  wallRight, wallBottom, yesOrNo, goal)
  }

  def withBottom(yesOrNo : Boolean = true): Cell ={
    new Cell(wallTop,  wallRight, yesOrNo, wallLeft, goal)
  }

  def withGoal(g : Goal): Cell ={
    new Cell(wallTop, wallRight,wallBottom, wallLeft ,g)
  }
}

object Cell{
  def Empty = {new Cell(false, false, false, false, null)}
}

