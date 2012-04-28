package models

import util.Random

class Game(val board:Board, val goal: Goal,val durationInSeconds:Int){

  private var robotsList:List[Robot] = Nil
  def robots = {
     if(robotsList.isEmpty){
       robotsList = randomRobots(board)
     }
    robotsList
  }

  def randomRobots(board:Board): List[Robot] = {
    var robots = List[Robot]()
    for(c <- Color.values){
      var posX:Int = getRandomPosition(board.width)
      var posY:Int = getRandomPosition(board.height)
      while((posX==7 || posX == 8) && (posY==7 || posY == 8)){
        posX = getRandomPosition(board.width)
        posY = getRandomPosition(board.width)
      }
      robots ::=  new Robot(c,posX,posY)
    }
    robots
  }

  def getRandomPosition(maxValue:Int) = {
    Random.nextInt(maxValue)
  }
}
object Game {
  def randomGame():Game = {
    new Game(DefaultBoard, Goal.randomGoal(), 120);
  }

}
