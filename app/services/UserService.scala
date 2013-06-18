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

  //region Parsers
  /**
   * Parse a User from a ResultSet
   */
  val userParser = {
    get[String]("users.id") ~
      get[String]("users.provider") ~
      get[String]("users.firstName") ~
      get[String]("users.lastName") ~
      get[String]("users.fullName") ~
      get[Option[String]]("users.email") ~
      get[Option[String]]("users.avatarUrl") ~
      get[String]("users.authMethod") ~
      get[Boolean]("users.isAdmin") ~
      get[String]("users.locale") ~
      //TODO OAUth1
      //TODO OAuth2
      // PasswordInfo part
      get[Option[String]]("users.hasher") ~
      get[Option[String]]("users.password") ~
      get[Option[String]]("users.salt") map {
      case id ~ provider ~ firstName ~ lastName ~ fullName ~ email ~ avatarUrl ~ authMethod ~ isAdmin ~ locale ~ hasher ~ password ~ salt =>
        DbUser(UserId(id, provider), firstName, lastName, fullName, email, avatarUrl, AuthenticationMethod(authMethod), isAdmin, locale, None, None, toPasswordInfo(hasher, password, salt))
    }
  }

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

  def toPasswordInfo(hasher: Option[String], password: Option[String], salt: Option[String]): Option[PasswordInfo] = password match {
    case None => None
    case Some(pass) => hasher match {
      case None => None
      case Some(hash) => Some(PasswordInfo(hash, pass, salt))
    }
  }

  //endregion

  //region User
  /**
   * Finds a user that matches the specified id
   *
   * @param id the user id
   * @return an optional user
   */
  def find(id: UserId): Option[DbUser] = {
    DB.withConnection {
      implicit connection =>
        SQL( """
        SELECT id, provider, firstName, lastName, fullName, email, avatarUrl, authMethod, isAdmin, locale, hasher, password, salt
        FROM users
        WHERE upper(id) = upper({id})
         and upper(provider) = upper({provider})
             """
        ).on(
          'id -> id.id,
          'provider -> id.providerId
        ).as(userParser.singleOpt)
    }
  }

  /**
   * Finds a user by email and provider id.
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation.
   *
   * @param email - the user email
   * @param provider - the provider id
   * @return
   */
  def findByEmailAndProvider(email: String, provider: String): Option[DbUser] = {
    DB.withConnection {
      implicit connection =>
        SQL( """
        SELECT id, provider, firstName, lastName, fullName, email, avatarUrl, authMethod, isAdmin, locale, hasher, password, salt
        FROM users
        WHERE upper(email) = upper({email})
         and upper(provider) = upper({provider})
             """
        ).on(
          'email -> email,
          'provider -> provider
        ).as(userParser.singleOpt)
    }
  }

  /**
   * Saves the user.  This method gets called when a user logs in.
   * This is your chance to save the user information in your backing store.
   * @param user the user to save
   */
  def save(user: Identity): DbUser = {
    find(user.id) match {
      case Some(dbUser: DbUser) => update(dbUser)
      case _ => create(user)
    }
  }

  /**
   * Create a DbUser.
   *
   * @return None if user cannot be created
   */
  private def update(user: DbUser): DbUser = {
    DB.withConnection {
      implicit connection =>
        withUserValues(SQL(
          """
           UPDATE users
            SET firstName = {firstName}, lastName = {lastName}, fullName = {fullName}, email =  {email}, avatarUrl = {avatarUrl}, authMethod = {authMethod}, isAdmin = {isAdmin}, locale = {locale}, hasher = {hasher}, password = {password}, salt = {salt}
            WHERE upper(id) = upper({id})
             and upper(provider) = upper({provider})
          """
        ), user).executeUpdate()
    }

    find(user.id).get
  }

  /**
   * Update a DbUser.
   *
   * @return None if user cannot be updated
   */
  private def create(user: Identity): DbUser = {
    DB.withConnection {
      implicit connection =>
      // Insert the user
        withIdentityValues(SQL(
          """
           INSERT INTO users (
             id, provider, firstName, lastName, fullName, email, avatarUrl, authMethod, hasher, password, salt
           )
           values (
             {id}, {provider}, {firstName}, {lastName}, {fullName}, {email}, {avatarUrl}, {authMethod}, {hasher}, {password}, {salt}
           )
          """
        ), user).executeInsert()
    }

    find(user.id).get
  }

  private def withUserValues(query: SqlQuery, user: DbUser): SimpleSql[Row] = {
    withIdentityValues(query, user).on(
      'isAdmin -> user.isAdmin,
      'locale -> user.locale
    )
  }

  private def withIdentityValues(query: SqlQuery, user: Identity): SimpleSql[Row] = {
    query.on(
      'id -> user.id.id,
      'provider -> user.id.providerId,
      'firstName -> user.firstName,
      'lastName -> user.lastName,
      'fullName -> user.fullName,
      'email -> user.email,
      'avatarUrl -> user.avatarUrl.getOrElse("/assets/images/symbols/robot.png"),
      'authMethod -> user.authMethod.method,
      // PasswordInfo part
      'hasher -> user.passwordInfo.map(_.hasher),
      'password -> user.passwordInfo.map(_.password),
      'salt -> user.passwordInfo.map(_.salt)
    )
  }

  //endregion User

  //region token
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

  //endregion   token
}
