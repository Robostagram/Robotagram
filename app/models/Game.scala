package models

import models.Color._
import collection.immutable.HashMap
import java.util.UUID
import scala._
import java.util.{Date,Random}

class Game(val id:String, val board:Board, val goal: Goal,val startDate:Date, val endDate:Date, val robots:HashMap[Color, Robot]){
  val uuid:String = id;
  val endTime:Long = endDate.getTime;
  val durationInSeconds = ((endDate.getTime - startDate.getTime)/1000.0).toInt

  def isDone:Boolean = System.currentTimeMillis() > endTime

  def secondsLeft(): Int = ((endTime - System.currentTimeMillis())/1000.0).toInt

  def percentageDone():Int = 100 - ((endTime - System.currentTimeMillis()).toDouble / (durationInSeconds*10).toDouble).round.toInt

  def remainingMilliseconds():Long = endTime - System.currentTimeMillis()

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
  val NB_BOARDS_IN_DB = 6 // booooh - make it dynamic when/if we allow to create boards
  def randomGame():Game = {
    var idOfBoardToLoad = new Random().nextInt(NB_BOARDS_IN_DB) + 1
    var board = Board.loadById(idOfBoardToLoad).get
    var robots = Board.randomRobots(board)

    val originalTimeStamp = System.currentTimeMillis()
    val startDate = new Date(originalTimeStamp)
    val endDate = new Date (originalTimeStamp + 1000 * DEFAULT_GAME_DURATION)

    new Game(UUID.randomUUID().toString, board, Goal.randomGoal(), startDate, endDate, robots)
  }

  def load(roomName:String, gameId:String):Option[Game] = {
    DbGame.findByRoomAndId(roomName, gameId).map{ dbGame =>
      Some(fromDb(dbGame))
    }
    .getOrElse(None)
  }

  private def fromDb(dbGame:DbGame):Game = {
    var board = Board.loadById(dbGame.board_id).get
    var goal = new Goal(Color.withName(dbGame.goal_color), Symbol.withName(dbGame.goal_symbol))
    //load the robots
    var robots = new HashMap[Color, Robot]()
    robots += ((Color.Blue, new Robot(Color.Blue, dbGame.robot_blue_x, dbGame.robot_blue_y)))
    robots += ((Color.Red, new Robot(Color.Red, dbGame.robot_red_x, dbGame.robot_red_y)))
    robots += ((Color.Green, new Robot(Color.Green, dbGame.robot_green_x, dbGame.robot_green_y)))
    robots += ((Color.Yellow, new Robot(Color.Yellow, dbGame.robot_yellow_x, dbGame.robot_yellow_y)))

    new Game(dbGame.id, board, goal, dbGame.created_on, dbGame.valid_until, robots)
  }

  def getActiveInRoomOrCreate(roomId:Long) : Game = {
    var gameFromDb = DbGame.getActiveInRoomOrCreate(roomId, () => {
      var idOfBoardToLoad = new Random().nextInt(6) + 1
      var b = Board.loadById(idOfBoardToLoad).get
      var g = Goal.randomGoal()
      var robots = Board.randomRobots(b)
      DbGame.prepareGameToStore(roomId, Game.DEFAULT_GAME_DURATION,b, g, robots)
    })
    fromDb(gameFromDb)
  }

}

