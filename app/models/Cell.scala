package models

import models.Direction._

class Cell(val wallTop: Boolean, val wallRight: Boolean, val wallBottom: Boolean, val wallLeft: Boolean, val goal: Goal) {

  def withRight(yesOrNo: Boolean = true): Cell = {
    new Cell(wallTop, yesOrNo, wallBottom, wallLeft, goal)
  }

  def withTop(yesOrNo: Boolean = true): Cell = {
    new Cell(yesOrNo, wallRight, wallBottom, wallLeft, goal)
  }

  def withLeft(yesOrNo: Boolean = true): Cell = {
    new Cell(wallTop, wallRight, wallBottom, yesOrNo, goal)
  }

  def withBottom(yesOrNo: Boolean = true): Cell = {
    new Cell(wallTop, wallRight, yesOrNo, wallLeft, goal)
  }

  def withGoal(g: Goal): Cell = {
    new Cell(wallTop, wallRight, wallBottom, wallLeft, g)
  }

  def rotate90deg(): Cell = {
    new Cell(wallLeft, wallTop, wallRight, wallBottom, goal)
  }
  
  def hasWall(direction: Direction): Boolean = direction match {
    case Up => wallTop
    case Left => wallLeft
    case Down => wallBottom
    case Right => wallRight
  }
}

object EmptyCell extends Cell(false, false, false, false, null)



