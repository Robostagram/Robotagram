package models

import models.Color._
import models.Phase._
import collection.immutable.Map
import collection.immutable.HashMap
import java.util.UUID
import scala._
import collection.mutable
import java.util.{Date,Random}
import play.api.Logger
import controllers.Gaming
import controllers.MessageID.TIME_UP


class Game(val id:String, val roomId: Long, val board:Board, val goal: Goal, val startDate:Date, val endDate:Date, val robots:Map[Color, Robot], val gamePhase: Phase){

  //init: register as active game
  Game.activeGames.add((id, roomId, gamePhase))

  private val endTime:Long = endDate.getTime;
  
  val durationInSeconds = ((endDate.getTime - startDate.getTime)/1000.0).toInt

  def isDone(): Boolean = System.currentTimeMillis() > endTime
  
  def secondsLeft(): Int = ((endTime - System.currentTimeMillis())/1000.0).toInt

  def percentageLeft():Int = ((endTime - System.currentTimeMillis()).toDouble / (durationInSeconds*10).toDouble).round.toInt
  
  private def done() {
    if (gamePhase != SHOW_SOLUTION) {
      // switch to next game phase unless already at the end of the cycle
      val endedGame = toPhase(SHOW_SOLUTION)
      endedGame.timer.start()
    }
    // announce game dead to the rooms
    Gaming.notifyRoom(DbRoom.findById(roomId).get.name, TIME_UP, Seq())
  }
  
  // game init
  val timer = new Thread(new TimerThread(roomId + "_" + id + "_" + gamePhase, _ => !isDone, _ => !Game.activeGames.contains((id, roomId, gamePhase)), _ => done))
  
  // returns a robot if there's one at the specified coordinates
  def getRobot(col: Int, row: Int): Robot = {
    for(r: Robot <- robots.values) {
      if(r.col == col && r.row == row) {
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
      Logger.info("invalid move attempted, expected color :"
            + robot.color + " was :" + movement.color)
      return robots
    } else {
      //println("moving: " + movement.color)
      if(robot.col != movement.col || robot.row != movement.row) {
        Logger.info("invalid move attempted, expected :"
            + robot.color + ", (" + robot.col + ", " + robot.row + "),"
            + " was :" + movement.color + ", (" + movement.col + ", " + movement.row + ")")
        return robots
      }
      val diffs = movement.direction match {
        case Direction.Up => (0,-1)
        case Direction.Left => (-1,0)
        case Direction.Down => (0,1)
        case Direction.Right => (1,0)
      }
      val xDiff = diffs._1
      val yDiff = diffs._2
      var newX = robot.col+xDiff
      var newY = robot.row+yDiff
      while(!(board.getCell(robot.col, robot.row).hasWall(movement.direction) || mutatingRobots.values.foldLeft(false)((res, rob) => res || (rob.col == newX && rob.row == newY)))) {
        //println(newX + ", " + newY)
        robot = new Robot(movement.color, newX, newY)
        newX = robot.col+xDiff
        newY = robot.row+yDiff
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
                  goalPosition._1 == robot.col &&
                  goalPosition._2 == robot.row
    case head :: tail => validate(move(robotss, head), tail)
  }

  /**
   * If the game phase parameter is the same as the current game phase, simply returns the same game.
   * If the game phase is different, creates a new game, identical to this but for the start and
   * end dates and the game phase. The new game is started immediately and persisted and activated.
   * The old game is deactivated.
   */
  def toPhase(gamePhase: Phase): Game = {
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
      val updatedGame = new Game(this.id, this.roomId, this.board, this.goal, startDate, endDate, this.robots, gamePhase)
      val dbGame = DbGame.fromGame(roomId, updatedGame)
      if (!DbGame.updatePhase(dbGame)) {
        Logger.error("unable to update game with id " + dbGame.id + " in persistence")
      } else {
        Logger.debug("game with id " + dbGame.id + " and phase " + this.gamePhase + " rendered obsolete")
        Game.activeGames.remove((id, roomId, this.gamePhase))
      }
      updatedGame
    }
  }
  
}

object Game {

  val activeGames = new mutable.HashSet[(String, Long, Phase)]

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

    new Game(dbGame.id, dbGame.room_id, board, goal, dbGame.created_on, dbGame.valid_until, robots, Phase.withName(dbGame.phase))
  }

  val DEFAULT_GAME_1_DURATION = 180
  val DEFAULT_GAME_2_DURATION = 60
  val DEFAULT_SHOW_SOL_DURATION = 10 // very small as nothing to be seen in this phase yet
  val NB_BOARDS_IN_DB = 6 // booooh - make it dynamic when/if we allow to create boards

  // get the active game in the room or create a random one
  def getActiveInRoomOrCreateRandom(roomId:Long) : Game = {
    DbGame.getActiveDbGame(roomId) match {
      case Some(dbGame) => fromDb(dbGame)
      case None => {
        val idOfBoardToLoad = new Random().nextInt(NB_BOARDS_IN_DB) + 1
        val theBoard = Board.loadById(idOfBoardToLoad).get
        val theGoal = Goal.randomGoal()
        val theRobots = Board.randomRobots(theBoard)
        val dbGame = DbGame.prepareGameToStore(roomId, DEFAULT_GAME_1_DURATION, theBoard, theGoal, theRobots, GAME_1)
        DbGame.insertGame(dbGame)
        val game = fromDb(dbGame)
        game.timer.start()
        game
      }
    }
  }

}
