package models

import play.api.Play.current
import anorm._
import java.util.Date
import play.api.db.DB
import anorm.SqlParser._
import scala.Some

case class DbScore(id: Pk[Long], score: Int, dateSubmitted:Date, playerName:String)

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
  def insert(gameId:String, userId:Long, score:Int, solution:String): Option[Long] = {
    DB.withConnection { implicit connection =>

    // Get the score id
    val theId: Long = SQL("select nextval('scores_seq')").as(scalar[Long].single)

    // Insert the user
    SQL(
      """
     insert into scores (
       id, submitted_on, solution, score, game_id, user_id
     )
     values (
       {id}, {submittedOn}, {solution}, {score}, {gameId}, {userId}
     )
      """
    ).on(
      'id -> theId,
      'submittedOn -> new Date(),
      'solution -> solution,
      'score -> score,
      'gameId -> gameId,
      'userId -> userId
    ).executeInsert()

    Some(theId)
    }
  }
}
