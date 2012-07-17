package models

import play.api.Play.current
import anorm.SqlParser._
import anorm._
import anorm.~
import collection.immutable.HashMap
import java.util.{UUID, Date}
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
                  board_id:Long)

object DbGame{

  //prepare a game for persistence
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

    new DbGame( uuid.toString, startDate, endDate,goal_symbol, goal_color, robot_blue_x, robot_blue_y, robot_red_x, robot_red_y, robot_green_x, robot_green_y, robot_yellow_x, robot_yellow_y,roomId, board_id)
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
        robotYellowX, robotYellowY,
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
  def getActiveInRoomOrCreate(roomId:Long, creationCallBack: () => DbGame) : DbGame = {
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
        .getOrElse{
        // what we should insert
        val game = creationCallBack.apply()
        //create(game)
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
          'robot_green_x -> game.robot_green_x,
          'robot_green_y -> game.robot_green_y,
          'robot_yellow_x -> game.robot_yellow_x,
          'robot_yellow_y -> game.robot_yellow_y,
          'room_id -> game.room_id,
          'board_id -> game.board_id
        ).executeUpdate()

        game
      }
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

