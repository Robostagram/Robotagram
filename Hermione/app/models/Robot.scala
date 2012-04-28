package models

import models.Color._

class Robot(val color: Color, val posX:Int, val posY:Int) {
  def toLocation(x:Int, y:Int) : Robot = {
     new Robot(color, x, y);
  }
}
