package models

import play.api.Play.current
import anorm.SqlParser._
import anorm._
import anorm.~
import collection.immutable.Map
import java.util.{UUID, Date}
import models.Phase._
import models.Color.Color
import play.api.db.DB

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
                  board_id:Long,
                  phase:String)

object DbGame{

  //prepare a game for persistence
  def prepareGameToStore(roomId:Long, durationInSeconds:Long, board:Board, goal:Goal, robots:Map[Color, Robot], gamePhase: Phase):DbGame = {
    val uuid = UUID.randomUUID()
    val originalTimeStamp = System.currentTimeMillis()
    val startDate = new Date(originalTimeStamp)
    val endDate = new Date (originalTimeStamp + 1000 * durationInSeconds)

    val board_id = board.id

    val (col, sym, blueX, blueY, redX, redY, greenX, greenY, yellowX, yellowY) = dbForm(goal, robots)
    
    new DbGame( uuid.toString, startDate, endDate, sym, col, blueX, blueY, redX, redY, greenX, greenY, yellowX, yellowY, roomId, board_id, gamePhase.toString)
  }
  
  def fromGame(roomId: Long, game: Game): DbGame = {
    val (col, sym, blueX, blueY, redX, redY, greenX, greenY, yellowX, yellowY) = dbForm(game.goal, game.robots)
    new DbGame(game.id, game.startDate, game.endDate, sym, col, blueX, blueY, redX, redY, greenX, greenY, yellowX, yellowY, roomId, game.board.id, game.gamePhase.toString)    
  } 
  
  private def dbForm(goal: Goal, robots: Map[Color, Robot]): (String, String, Int, Int, Int, Int, Int, Int, Int, Int) = {
    (
        goal.color.toString,
        goal.symbol.toString,
        robots.get(Color.Blue).get.col,
        robots.get(Color.Blue).get.row,
        robots.get(Color.Red).get.col,
        robots.get(Color.Red).get.row,
        robots.get(Color.Green).get.col,
        robots.get(Color.Green).get.row,
        robots.get(Color.Yellow).get.col,
        robots.get(Color.Yellow).get.row
    )
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
      get[Long]("games.board_id") ~
      get[String]("games.phase") map {
      case id~createdOn~validUntil
        ~goalSymbol~goalColor
        ~robotBlueX~robotBlueY
        ~robotRedX~robotRedY
        ~robotGreenX~robotGreenY
        ~robotYellowX~robotYellowY
        ~roomId~boardId~phase
      => DbGame(id, createdOn, validUntil,
        goalSymbol, goalColor,
        robotBlueX, robotBlueY,
        robotRedX, robotRedY,
        robotGreenX, robotGreenY,
        robotYellowX, robotYellowY,
        roomId, boardId, phase
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
  
  def updatePhase(game: DbGame): Boolean = {
    DB.withConnection { implicit connection =>
      val res = SQL(
        """
           UPDATE games
           SET phase = {phase}, created_on = {created_on}, valid_until = {valid_until}
           WHERE id = {id}
        """
      ).on(
        'phase -> game.phase,
        'created_on -> game.created_on,
        'valid_until -> game.valid_until,
        'id -> game.id
      ).executeUpdate()

      res > 0 // true if modified something . false if not
    }
  }

  def insertGame(game: DbGame) {
    //create(game)
    DB.withConnection { implicit connection =>
    SQL(
      """
      insert into games (
        id, created_on, valid_until, goal_symbol, goal_color,
        robot_blue_x, robot_blue_y, robot_red_x, robot_red_y,
        robot_green_x, robot_green_y, robot_yellow_x, robot_yellow_y,
        room_id, board_id, phase
      )
      values (
        {id}, {created_on}, {valid_until}, {goal_symbol}, {goal_color},
        {robot_blue_x}, {robot_blue_y}, {robot_red_x}, {robot_red_y},
        {robot_green_x}, {robot_green_y}, {robot_yellow_x}, {robot_yellow_y},
        {room_id}, {board_id}, {phase}
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
      'robot_green_x -> game.robot_green_x,
      'robot_green_y -> game.robot_green_y,
      'robot_yellow_x -> game.robot_yellow_x,
      'robot_yellow_y -> game.robot_yellow_y,
      'room_id -> game.room_id,
      'board_id -> game.board_id,
      'phase -> game.phase
    ).executeUpdate()
    }
  }

  // return the active game in a room or create it using the construction method provided
  def getActiveDbGame(roomId:Long) : Option[DbGame] = {
    DB.withTransaction { implicit conn =>
    // if there is an active game
      SQL("""
        SELECT games.*
        FROM games
        WHERE games.room_id = {roomId}
          and games.valid_until > {now}
          """
      ).on(
        'roomId -> roomId,
        'now -> new Date()
      ).as(DbGame.fullRow.singleOpt)
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
          select *
          from games
          where room_id = {roomId}
          order by created_on DESC
          limit {limit}
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

