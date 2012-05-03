package models

import models.Color._
import models.Symbol._
import util.Random

class Goal(val color: Color, val symbol: Symbol)

object Goal {
  def randomGoal(): Goal = {
    new Goal(Color(Random.nextInt(Color.values.size)), Symbol(Random.nextInt(Symbol.values.size - 1)))// -1 to remove the sun
  }
}
