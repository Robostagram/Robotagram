package models

import play.api.Play.current
import anorm._
import java.util.Date
import play.api.db.DB
import anorm.SqlParser._
import scala.Some
import scala.collection.mutable.HashMap
import scala.collection.Map
import securesocial.core.UserId

case class DbScore(id: Pk[Long], score: Int, dateSubmitted:Date, playerName:String)
case class DbRoomScores(score: Int, gameId: String, playerName:String, date: Date)

object DbRoomScores{
  
  private val simple = {
      get[Int]("scores.score") ~
      get[String]("scores.game_id") ~
      get[String]("users.name") ~
      get[Date]("scores.submitted_on") map {
      case score~gameId~playerName~date => DbRoomScores(score, gameId, playerName, date)
    }
  }
  
  private def scoresInRoom(roomId: Long): Seq[DbRoomScores] = {
    DB.withConnection { implicit connection =>
      SQL("""
          select scores.score, scores.game_id, users.name, scores.submitted_on
          from games
          inner join scores
            on scores.game_id = games.id
          inner join users
            on scores.user_id = users.id
              and scores.user_provider = users.provider
          where games.room_id = {roomId}
          """
      ).on(
        'roomId -> roomId
      ).as(DbRoomScores.simple *)
    }
  }
  
  def winnersOfRoomId(roomId: Long): Map[String, (String, Int, Date)] = scoresInRoom(roomId).foldLeft(Map[String, (String, Int, Date)]()) {
    // fold to find the author of shortest and first solution of each game
    (map: Map[String, (String, Int, Date)], gameScore: DbRoomScores) => {
      val gameId = gameScore.gameId
      map.get(gameId) match  {
        case None => map + (gameId -> (gameScore.playerName, gameScore.score, gameScore.date))
        case Some((playerName, score, date)) => if (score < gameScore.score || (score == gameScore.score && date.before(gameScore.date)) ) {
          map
        } else {
          map + (gameId -> (gameScore.playerName, gameScore.score, gameScore.date))
        }
      }
    }
  }

}

object DbScore{

  /**
   * Parse a score from a ResultSet
   */
  val simple = {
      get[Pk[Long]]("scores.id") ~
      get[Int]("scores.score") ~
      get[Date]("scores.submitted_on") ~
      get[String]("users.name") map {
      case id~score~submittedOn~playerName => DbScore(id, score, submittedOn, playerName)
    }
  }

  /**
   * Retrieve all scores in a game, highest scores first and latest submitted ascending
   */
  def findByGame(gameId:String): Seq[DbScore] = {
    DB.withConnection { implicit connection =>
      SQL("""
          select scores.id, scores.score, scores.submitted_on, users.name
          from scores
          inner join users
            on scores.user_id = users.id
              and scores.user_provider = users.provider
          where scores.game_id = {gameId}
          order by scores.score DESC, submitted_on ASC
          """
      ).on(
        'gameId -> gameId
      ).as(DbScore.simple *)
    }
  }
  
  /**
   * Insert a score of someone in a game.
   */
  def insert(gameId:String, userId:UserId, score:Int, solution:String): Option[Long] = {
    DB.withConnection { implicit connection =>

    // Get the score id
    val theId: Long = SQL("select nextval('scores_seq')").as(scalar[Long].single)

    // Insert the user
    SQL(
      """
     insert into scores (
       id, submitted_on, solution, score, game_id, user_id, user_provider
     )
     values (
       {id}, {submittedOn}, {solution}, {score}, {gameId}, {userId}, {user_provider}
     )
      """
    ).on(
      'id -> theId,
      'submittedOn -> new Date(),
      'solution -> solution,
      'score -> score,
      'gameId -> gameId,
      'userId -> userId.id,
      'user_provider -> userId.providerId
    ).executeInsert()

    Some(theId)
    }
  }
}
