package models

import models.Color._
import models.Phase._
import collection.immutable.Map
import collection.immutable.HashMap
import java.util.UUID
import scala._
import java.util.{Date,Random}

class Game(val id:String, val board:Board, val goal: Goal, val startDate:Date, val endDate:Date, val robots:Map[Color, Robot], val gamePhase: Phase){
  private val endTime:Long = endDate.getTime;
  val durationInSeconds = ((endDate.getTime - startDate.getTime)/1000.0).toInt

  def isDone:Boolean = System.currentTimeMillis() > endTime

  def secondsLeft(): Int = ((endTime - System.currentTimeMillis())/1000.0).toInt

  def percentageLeft():Int = ((endTime - System.currentTimeMillis()).toDouble / (durationInSeconds*10).toDouble).round.toInt
  
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
  private def move(mutatingRobots: Map[Color, Robot], movement: Movement): Map[Color, Robot] = {
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
  private def validate(robotss: Map[Color, Robot], solution: List[Movement]): Boolean = solution match {
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

  def withPhase(gamePhase: Phase): Game = {
    if (this.gamePhase == gamePhase) this
    else {
      val duration = gamePhase match {
        case GAME_1 => Game.DEFAULT_GAME_1_DURATION
        case GAME_2 => Game.DEFAULT_GAME_2_DURATION
        case SHOW_SOLUTION => Game.DEFAULT_SHOW_SOL_DURATION
      }
      val originalTimeStamp = System.currentTimeMillis()
      val startDate = new Date(originalTimeStamp)
      val endDate = new Date (originalTimeStamp + 1000 * duration)
      new Game(this.id, this.board, this.goal, startDate, endDate, this.robots, gamePhase)
    }
  }
  
}

object Game {

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

    new Game(dbGame.id, board, goal, dbGame.created_on, dbGame.valid_until, robots, Phase.withName(dbGame.phase))
  }

  val DEFAULT_GAME_1_DURATION = 180
  val DEFAULT_GAME_2_DURATION = 60
  val DEFAULT_SHOW_SOL_DURATION = 60
  val NB_BOARDS_IN_DB = 6 // booooh - make it dynamic when/if we allow to create boards

  // get the active game in the room or create a random one
  def getActiveInRoomOrCreateRandom(roomId:Long) : Game = {
    val gameFromDb = DbGame.getActiveInRoomOrCreate(roomId, () => {
      val idOfBoardToLoad = new Random().nextInt(NB_BOARDS_IN_DB) + 1
      val theBoard = Board.loadById(idOfBoardToLoad).get
      val theGoal = Goal.randomGoal()
      val theRobots = Board.randomRobots(theBoard)
      DbGame.prepareGameToStore(roomId, DEFAULT_GAME_1_DURATION, theBoard, theGoal, theRobots, GAME_1)
    })
    fromDb(gameFromDb)
  }

}

