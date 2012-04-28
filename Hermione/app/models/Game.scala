package models

import util.Random

class Game(val board:Board, val robots:List[Robot], val goal: Goal, val durationInSeconds:Int)

object Game {
  def randomGame():Game = {
    new Game(DefaultBoard, randomRobots(DefaultBoard), Goal.randomGoal(), 120);
  }

  def randomRobots(board:Board): List[Robot] = {
    var robots = List[Robot]()
    for(c <- Color.values){
      val posX:Int = Random.nextInt(board.width)
      val posY:Int = Random.nextInt(board.height)
      robots ::=  new Robot(c,posX,posY)
    }
    robots
  }
}
