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
      while(unacceptableValue(posX, posY, board.width, board.height)){
        posX = getRandomPosition(board.width)
        posY = getRandomPosition(board.width)
      }
      robots ::=  new Robot(c,posX,posY)
    }
    robots
  }
  def unacceptableValue(posX:Int, posY:Int, w:Int,h:Int):Boolean = {
    var resultW:Boolean = true;
    var resultH:Boolean = true;
    var midH = h/2;
    var midW = w/2;
    if(w % 2 ==0){
         resultW = posX==midW||posX==midW-1
    } else{
       resultW = posX==(w-1)/2
    }
    if(h % 2 ==0){
      resultH = posY==midH||posY==midH-1
    } else{
      resultH = posY==(h-1)/2
    }
    resultW && resultH
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
