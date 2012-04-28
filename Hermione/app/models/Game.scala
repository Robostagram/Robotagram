package models

import util.Random

class Game(val board:Board, val robots:List[Robot], goal: Goal, durationInSeconds:Int)

object Game {
  def randomGame():Game = {
    new Game(DefaultBoard, randomRobots(DefaultBoard), Goal.randomGoal(), 120);
  }

  def randomRobots(board:Board): List[Robot] = {
    var robots = List[Robot]()
    for(c <- Color.values){
      val posX:Int = getRandomPosition(board.width)
      val posY:Int = getRandomPosition(board.height)
      robots ::=  new Robot(c,posX,posY)
    }
    robots
  }

  def getRandomPosition(maxValue:Int) = {
    Random.nextInt(maxValue)
  }
}
