package models

import util.Random

class Game(val board:Board, val robots:List[Robot], goal: Goal, durationInSeconds:Int)

object Game {
  def randomGame():Game = {
    val board = Board.boardFromFile("app/resources/Standard.board").randomizeQuarters()
    new Game(board, randomRobots(board), Goal.randomGoal(), 120);
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
