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

class UserService(application: Application) extends UserServicePlugin(application) {

  //region Parsers
  /**
   * Parse a User from a ResultSet
   */
  val simple = {
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

  def toPasswordInfo(hasher: Option[String], password: Option[String], salt: Option[String]): Option[PasswordInfo] = password match {
    case None => None
    case Some(pass) => hasher match {
      case None => None
      case Some(hash) => Some(PasswordInfo(hash, pass, salt))
    }
  }

  //endregion

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
        ).as(simple.singleOpt)
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
        ).as(simple.singleOpt)
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
      case _ => create(user.asInstanceOf) //TODO is it really
    }
    find(user.id).get
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
  private def create(user: DbUser): DbUser = {
    DB.withConnection {
      implicit connection =>
      // Insert the user
        withUserValues(SQL(
          """
           INSERT INTO users (
             id, provider, firstName, lastName, fullName, email, avatarUrl, authMethod, isAdmin, locale, hasher, password, salt
           )
           values (
             {id}, {provider}, {firstName}, {lastName}, {fullName}, {email}, {avatarUrl}, {authMethod}, {isAdmin}, {locale}, {hasher}, {password}, {salt}
           )
          """
        ), user).executeInsert()
    }

    find(user.id).get
  }

  private def withUserValues(query: SqlQuery, user: DbUser): SimpleSql[Row] = {
    query.on(
      'id -> user.id.id,
      'provider -> user.id.providerId,
      'firstName -> user.firstName,
      'lastName -> user.lastName,
      'fullName -> user.fullName,
      'email -> user.email,
      'avatarUrl -> user.avatarUrl.getOrElse("/assets/images/symbols/robot.png"),
      'authMethod -> user.authMethod,
      'isAdmin -> user.isAdmin,
      'locale -> user.locale,
      // PasswordInfo part
      'hasher -> user.passwordInfo.map(_.hasher),
      'password -> user.passwordInfo.map(_.password),
      'salt -> user.passwordInfo.map(_.salt)
    )
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
    // implement me
  }


  /**
   * Finds a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param token the token id
   * @return
   */
  def findToken(token: String): Option[Token] = {
    // implement me
    None
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
    // implement me
  }

  /**
   * Deletes all expired tokens
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   */
  def deleteExpiredTokens() {
    // implement me
  }
}
