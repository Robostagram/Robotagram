package models

import models.Color._
import scala.util.Random
import scala.collection.immutable.HashSet
import scala.collection.immutable.HashMap
import java.util.UUID
import scala._

class Game(val board:Board, val goal: Goal,val durationInSeconds:Int){
  val uuid:String = UUID.randomUUID().toString;
  val endTime:Long = System.currentTimeMillis() + durationInSeconds*1000;
  
  private var robotsList:HashMap[Color, Robot] = null
  def robots = {
    if(robotsList == null){
      robotsList = randomRobots(board)
    }
    robotsList
  }

  def isDone:Boolean = System.currentTimeMillis() > endTime

  def secondsLeft(): Int = ((endTime - System.currentTimeMillis())/1000.0).toInt

  def percentageDone():Int = 100 - ((endTime - System.currentTimeMillis()).toDouble / (durationInSeconds*10).toDouble).round.toInt

  def remainingMilliseconds():Long = endTime - System.currentTimeMillis()

  def randomRobots(board:Board): HashMap[Color, Robot] = {
    var robots = new HashMap[Color, Robot]()
    var setPos:Set[Tuple2[Int,Int]] = new HashSet[Tuple2[Int,Int]];
	for(c <- Color.values){
      var posX:Int = getRandomPosition(board.width)
      var posY:Int = getRandomPosition(board.height)
      while(setPos.contains((posX,posY)) || unacceptableValue(posX, posY, board.width, board.height)){
        posX = getRandomPosition(board.width)
        posY = getRandomPosition(board.width)
      }
      setPos+=((posX,posY))
      robots += ((c, new Robot(c,posX,posY)))
    }
    robots
  }

  // detects middle (closed) square of the board, and goals
  def unacceptableValue(posX:Int, posY:Int, w:Int,h:Int):Boolean = {
    if(board.cells(posX)(posY).goal != null) {
	  return true
	}
    var resultW = true;
    var resultH = true;
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
  
  // returns a robot if there's one at the specified coordinates
  def getRobot(x: Int, y: Int): Robot = {
    for(r: Robot <- robots.values) {
      if(r.posX == x && r.posY == y) {
	    return r
	  }
    }
    null
  }
  
  // create an updated set of robots matching the specified movement and this game and the previous state of the set
  // detects walls and other robots
  def move(mutatingRobots: HashMap[Color, Robot], movement: Movement): HashMap[Color, Robot] = {
    var robot = mutatingRobots.getOrElse(movement.color, null)
	if(robot == null) {
	  return robots
	} else {
	  if(robot.posX != movement.x || robot.posY != movement.y) {
	    // TODO log invalid move?
	    return robots
	  }
	  val diffs = movement.direction match {
	    case Direction.Up => (-1,0)
	    case Direction.Left => (0,-1)
	    case Direction.Down => (1,0)
	    case Direction.Right => (0,1)
	  }
	  val xDiff = diffs._1
	  val yDiff = diffs._2
	  var newX = robot.posX+xDiff
	  var newY = robot.posY+yDiff
	  while(!(board.cells(robot.posX)(robot.posY).hasWall(movement.direction) || mutatingRobots.values.foldLeft(false)((res, rob) => res || (rob.posX == newX && rob.posY == newY)))) {
	    robot = new Robot(movement.color, newX, newY)
		newX = robot.posX+xDiff
	    newY = robot.posY+yDiff
	  } 
	  mutatingRobots.updated(movement.color, robot)
	}
  }

  def getRandomPosition(maxValue:Int) = Random.nextInt(maxValue)

  // validate a list of movements in this game, applying them from head to tail
  def validate(solution: List[Movement]): Boolean = {
    validate(robots, solution)
  }
  
  // apply a list of movements to this game + specified robots set
  // when all movements are applied, check the expected robot is on the matching goal
  private def validate(robotss: HashMap[Color, Robot], solution: List[Movement]): Boolean = solution match {
    case Nil => val goalPosition = board.findGoalPosition(goal)
	            val robot = robotss.get(goal.color) match {
				  case Some(r) => r
				  case None => //TODO log?
				               null
				}
				goalPosition != (-1, -1) &&
				  robot != null &&
				  goalPosition._1 == robot.posX &&
				  goalPosition._2 == robot.posY
    case head :: tail => validate(move(robotss, head), tail)
  }
}

object Game {
  val DEFAULT_GAME_DURATION = 120

  def randomGame():Game = new Game(Board.boardFromFile("app/resources/Standard.board").randomizeQuarters(), Goal.randomGoal(), DEFAULT_GAME_DURATION)
}
