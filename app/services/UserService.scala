package services

import play.api.Play.current
import play.api.Application
import securesocial.core._
import models.DbUser
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import securesocial.core.UserId
import anorm.~
import securesocial.core.providers.Token
import org.joda.time.DateTime

class UserService(application: Application) extends UserServicePlugin(application) {
  def find(id: UserId): Option[Identity] = DbUser.find(id)

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = DbUser.findByEmailAndProvider(email, providerId)

  def save(user: Identity): Identity = DbUser.save(user)

  //region token

  val tokenParser = {
    get[String]("tokens.uuid") ~
      get[String]("tokens.email") ~
      get[Boolean]("tokens.isSignUp") ~
      get[Long]("tokens.creationTime") ~
      get[Long]("tokens.expirationTime") map {
      case uuid ~ email ~ isSignUp ~ creationTime ~ expirationTime =>
        Token(uuid, email, new DateTime(creationTime), new DateTime(expirationTime), isSignUp)
    }
  }

  /**
   * Saves a token.  This is needed for users that
   * are creating an account in the system instead of using one in a 3rd party system.
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param token The token to save
   * @return A string with a uuid that will be embedded in the welcome email.
   */
  def save(token: Token) {
    findToken(token.uuid) match {
      case Some(tok: Token) => update(token)
      case _ => create(token)
    }
  }

  private def update(token: Token) {
    DB.withConnection {
      implicit connection =>
        withTokenValues(SQL(
          """
           UPDATE tokens
            SET uuid = {uuid}, email = {email}, isSignUp = {isSignUp}, creationTime =  {creationTime}, expirationTime = {expirationTime}
            WHERE upper(uuid) = upper({uuid})
          """
        ), token).executeUpdate()
    }
  }

  private def create(token: Token) {
    DB.withConnection {
      implicit connection =>
      // Insert the user
        withTokenValues(SQL(
          """
           INSERT INTO tokens (
             uuid, email, isSignUp, creationTime, expirationTime
           )
           values (
             {uuid}, {email}, {isSignUp}, {creationTime}, {expirationTime}
           )
          """
        ), token).executeInsert()
    }
  }


  /**
   * Finds a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param uuid the token uuid
   * @return
   */
  def findToken(uuid: String): Option[Token] = {
    DB.withConnection {
      implicit connection =>
        SQL( """
        SELECT uuid, email, isSignUp, creationTime, expirationTime
        FROM tokens
        WHERE upper(uuid) = upper({uuid})
             """
        ).on(
          'uuid -> uuid
        ).as(tokenParser.singleOpt)
    }
  }

  /**
   * Deletes a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param uuid the token id
   */
  def deleteToken(uuid: String) {
    DB.withConnection {
      implicit connection =>
        SQL( """
        DELETE FROM tokens
        WHERE upper(uuid) = upper({uuid})
             """
        ).on(
          'uuid -> uuid
        )
    }
  }

  /**
   * Deletes all expired tokens
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   */
  def deleteExpiredTokens() {
    val now: Long = System.currentTimeMillis()
    DB.withConnection {
      implicit connection =>
        SQL( """
        DELETE FROM tokens
        WHERE expirationTime < {now}
             """
        ).on(
          'now -> now
        )
    }
  }

  private def withTokenValues(query: SqlQuery, token: Token): SimpleSql[Row] = {
    query.on(
      'uuid -> token.uuid,
      'isSignUp -> token.isSignUp,
      'creationTime -> token.creationTime.getMillis,
      'expirationTime -> token.expirationTime.getMillis,
      'email -> token.email
    )
  }

  //endregion token
}
