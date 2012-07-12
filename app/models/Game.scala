package models

import play.api.Play.current
import models.Color._
import models.Symbol._
import collection.immutable.HashMap
import java.util.UUID
import scala._
import anorm._
import anorm.SqlParser._
import play.api.db.DB
import anorm.~
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
      Board.loadById(dbGame.board_id).map{board =>
        var goal = new Goal(Color.withName(dbGame.goal_color), Symbol.withName(dbGame.goal_symbol))
        //load the robots
        var robots = new HashMap[Color, Robot]()
        robots += ((Color.Blue, new Robot(Color.Blue, dbGame.robot_blue_x, dbGame.robot_blue_y)))
        robots += ((Color.Red, new Robot(Color.Red, dbGame.robot_red_x, dbGame.robot_red_y)))
        robots += ((Color.Green, new Robot(Color.Green, dbGame.robot_green_x, dbGame.robot_green_y)))
        robots += ((Color.Yellow, new Robot(Color.Yellow, dbGame.robot_yellow_x, dbGame.robot_yellow_y)))

        new Game(dbGame.id, board, goal, dbGame.created_on, dbGame.valid_until, robots)
      }
    }
    .getOrElse(None)
  }

}


case class DbGame(id: String,
                  created_on : Date,
                  valid_until : Date,
                  goal_symbol:String,
                  goal_color:String,
                  robot_blue_x:Int,
                  robot_blue_y:Int,
                  robot_red_x:Int,
                  robot_red_y:Int,
                  robot_green_x:Int,
                  robot_green_y:Int,
                  robot_yellow_x:Int,
                  robot_yellow_y:Int,
                  room_id:Long,
                  board_id:Long)

object DbGame{

  //prepare
  def prepareGameToStore(roomId:Long, durationInSeconds:Long, board:Board, goal:Goal, robots:HashMap[Color, Robot]):DbGame = {
    val uuid = UUID.randomUUID()
    val originalTimeStamp = System.currentTimeMillis()
    val startDate = new Date(originalTimeStamp)
    val endDate = new Date (originalTimeStamp + 1000 * durationInSeconds)

    val goal_color = goal.color.toString
    val goal_symbol = goal.symbol.toString

    val board_id = board.id

    val robot_blue_x = robots.get(Color.Blue).get.posX
    val robot_blue_y = robots.get(Color.Blue).get.posY

    val robot_red_x = robots.get(Color.Red).get.posX
    val robot_red_y = robots.get(Color.Red).get.posY

    val robot_green_x = robots.get(Color.Green).get.posX
    val robot_green_y = robots.get(Color.Green).get.posY

    val robot_yellow_x = robots.get(Color.Yellow).get.posX
    val robot_yellow_y = robots.get(Color.Yellow).get.posY

    return new DbGame( uuid.toString, startDate, endDate,goal_symbol, goal_color, robot_blue_x, robot_blue_y, robot_red_x, robot_red_y, robot_green_x, robot_green_y, robot_yellow_x, robot_yellow_y,roomId, board_id)
  }

  // -- Parsers

  /**
   * Parse a DbGame from a ResultSet
   */
  val fullRow = {
    get[String]("games.id") ~
      get[Date]("games.created_on") ~
      get[Date]("games.valid_until") ~
      get[String]("games.goal_symbol") ~
      get[String]("games.goal_color") ~
      get[Int]("games.robot_blue_x") ~
      get[Int]("games.robot_blue_y") ~
      get[Int]("games.robot_red_x") ~
      get[Int]("games.robot_red_y") ~
      get[Int]("games.robot_green_x") ~
      get[Int]("games.robot_green_y") ~
      get[Int]("games.robot_yellow_x") ~
      get[Int]("games.robot_yellow_y") ~
      get[Long]("games.room_id") ~
      get[Long]("games.board_id") map {
      case id~createdOn~validUntil
        ~goalSymbol~goalColor
        ~robotBlueX~robotBlueY
        ~robotRedX~robotRedY
        ~robotGreenX~robotGreenY
        ~robotYellowX~robotYellowY
        ~roomId~boardId
      => DbGame(id, createdOn, validUntil,
        goalSymbol, goalColor,
        robotBlueX, robotBlueY,
        robotRedX, robotRedY,
        robotGreenX, robotGreenY,
        robotYellowX, robotYellowX,
        roomId, boardId
      )
    }
  }


  // -- Queries

  /**
   * Retrieve a Game from Id in a given room
   */
  def findByRoomAndId(roomName:String, gameId: String): Option[models.DbGame] = {
    DB.withConnection { implicit connection =>
      SQL("""
        SELECT games.*
        FROM rooms
        INNER JOIN games
          on games.room_id = rooms.id
          and games.id = {gameId}
        WHERE rooms.name ={roomName}
          """
      ).on(
        'roomName -> roomName,
        'gameId -> gameId
      ).as(DbGame.fullRow.singleOpt)
    }
  }

  def findActiveGameInRoom(roomName:String) : Option[DbGame] = {
    DB.withConnection { implicit connection =>
      SQL("""
        SELECT games.*
        FROM rooms
        INNER JOIN games
          on games.room_id = rooms.id
          and games.valid_until < now()
        WHERE rooms.name ={roomName}
          """
      ).on(
        'roomName -> roomName
      ).as(DbGame.fullRow.singleOpt)
    }
  }

  // return the active game in a room or create it using the construction method provided
  def getActiveInRoomOrCreate(roomName:String, creationCallBack: () => DbGame) : DbGame = {
    DB.withTransaction { implicit conn =>
      // si if there is an active game
      SQL("""
        SELECT games.*
        FROM rooms
        INNER JOIN games
          on games.room_id = rooms.id
          and games.valid_until > {now}
        WHERE rooms.name ={roomName}
          """
      ).on(
        'roomName -> roomName,
        'now -> new Date()
      ).as(DbGame.fullRow.singleOpt)
      .getOrElse{
        // what we should insert
        var game = creationCallBack.apply()
        create(game)
      }
    }
  }

  /**
   * Create a Game.
   */
  def create(game: DbGame): DbGame = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into games (
            id, created_on, valid_until, goal_symbol, goal_color,
            robot_blue_x, robot_blue_y, robot_red_x, robot_red_y,
            robot_green_x, robot_green_y, robot_yellow_x, robot_yellow_y,
            room_id, board_id
          )
          values (
            {id}, {created_on}, {valid_until}, {goal_symbol}, {goal_color},
            {robot_blue_x}, {robot_blue_y}, {robot_red_x}, {robot_red_y},
            {robot_green_x}, {robot_green_y}, {robot_yellow_x}, {robot_yellow_y},
            {room_id}, {board_id}
          )
        """
      ).on(
        'id -> game.id ,
        'created_on -> game.created_on,
        'valid_until -> game.valid_until,
        'goal_symbol -> game.goal_symbol,
        'goal_color -> game.goal_color,
        'robot_blue_x -> game.robot_blue_x,
        'robot_blue_y -> game.robot_blue_y,
        'robot_red_x -> game.robot_red_x,
        'robot_red_y -> game.robot_red_y,
        'robot_green_x -> game.robot_green_y,
        'robot_green_y -> game.robot_green_y,
        'robot_yellow_x -> game.robot_yellow_x,
        'robot_yellow_y -> game.robot_yellow_y,
        'room_id -> game.room_id,
        'board_id -> game.board_id
      ).executeUpdate()

      game

    }
  }




  /**
   * Retrieve all boards.
   */
  def findAll: Seq[DbGame] = {
    DB.withConnection { implicit connection =>
      SQL("""
          select *
          from games
          """
      ).as(DbGame.fullRow *)
    }
  }

  // list all games in room ... the newest first
  def getLatestGamesByRoom(roomId : Long, limit:Int) = {
    DB.withConnection { implicit connection =>
      SQL("""
          select TOP {limit} *
          from games
          where room_id = {roomId}
          order by created_on DESC
          """
      )
      .on(
        'roomId -> roomId,
        'limit -> limit
      )
      .as(DbGame.fullRow *)
    }
  }

}
