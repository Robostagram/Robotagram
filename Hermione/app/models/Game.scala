package models

import scala.util.Random
import scala.collection.immutable.HashSet
import java.util.UUID
import scala._

class Game(val board:Board, val goal: Goal,val durationInSeconds:Int){
  val uuid:String = UUID.randomUUID().toString;
  val endTime:Long = System.currentTimeMillis() + durationInSeconds*1000;
  var players:Set[Player] = new HashSet[Player];

  private var robotsList:List[Robot] = Nil
  def robots = {
    if(robotsList.isEmpty){
      robotsList = randomRobots(board)
    }
    robotsList
  }

  def isDone:Boolean = {
     System.currentTimeMillis() > endTime;
  }

  def withPlayer(user:String):Player = {
    val option:Option[Player] = players.find(player => player.name == user);
    if (option == None){
      val player:Player = new Player(user);
      players += player;
      return player
    }
    option.get
  }

  def percentageDone():Int = {
     ((endTime - System.currentTimeMillis()).toDouble / (durationInSeconds*10).toDouble).round.toInt;
  }

  def randomRobots(board:Board): List[Robot] = {
    var robots = List[Robot]()
    var setPos:Set[Tuple2[Int,Int]] = new HashSet[Tuple2[Int,Int]];
    for(c <- Color.values){
      var posX:Int = getRandomPosition(board.width)
      var posY:Int = getRandomPosition(board.height)
      while(setPos.contains((posX,posY)) || unacceptableValue(posX, posY, board.width, board.height)){
        posX = getRandomPosition(board.width)
        posY = getRandomPosition(board.width)
      }
      setPos+=((posX,posY))
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
  val DEFAULT_GAME_DURATION = 30;

  def randomGame():Game = {
    new Game(Board.boardFromFile("app/resources/Standard.board").randomizeQuarters(), Goal.randomGoal(), DEFAULT_GAME_DURATION);
  }

}
