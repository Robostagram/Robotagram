package models

import play.api.Play.current
import anorm._
import play.api.db.DB
import securesocial.core._
import securesocial.core.PasswordInfo
import securesocial.core.UserId
import securesocial.core.RequestWithUser
import securesocial.core.OAuth2Info
import securesocial.core.OAuth1Info
import play.api.mvc.AnyContent
import anorm.SqlParser._
import securesocial.core.OAuth1Info
import scala.Some
import securesocial.core.UserId
import anorm.~
import securesocial.core.OAuth2Info
import securesocial.core.PasswordInfo
import securesocial.core.RequestWithUser
import securesocial.core.providers.Token
import org.joda.time.DateTime

case class DbUser(id: UserId, firstName: String, lastName: String, fullName: String, email: Option[String],
                  avatarUrl: Option[String], authMethod: AuthenticationMethod, isAdmin: Boolean, locale: String,
                  oAuth1Info: Option[OAuth1Info] = None, oAuth2Info: Option[OAuth2Info] = None, passwordInfo: Option[PasswordInfo] = None) extends Identity

case class DbUserAccountActivation(id: UserId, firstName: String, lastName: String, fullName: String, email: Option[String],
                                   avatarUrl: Option[String], authMethod: AuthenticationMethod, isAdmin: Boolean, locale: Option[String],
                                   oAuth1Info: Option[OAuth1Info] = None, oAuth2Info: Option[OAuth2Info] = None, passwordInfo: Option[PasswordInfo] = None)

object DbUser {
  def fromRequest[A](locale: Option[String] = None, isAdmin: Boolean = false)(implicit request: RequestWithUser[A]): Option[DbUser] = {
    request.user.map(_ match {
      case user: DbUser => user
      case SocialUser(id, firstName, lastName, fullName, email, avatar, authMethod, oauth1, oauth2, passInfo) => DbUser(id, firstName, lastName, fullName, email, avatar, authMethod, isAdmin, locale.getOrElse("EN"), oauth1, oauth2, passInfo)
    })
  }

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

  private def toPasswordInfo(hasher: Option[String], password: Option[String], salt: Option[String]): Option[PasswordInfo] = password match {
    case None => None
    case Some(pass) => hasher match {
      case None => None
      case Some(hash) => Some(PasswordInfo(hash, pass, salt))
    }
  }
  //endregion

  /**
   * Finds all users
   */
  def findAll(): Seq[DbUser] = {
    DB.withConnection {
      implicit connection =>
        SQL( """
        SELECT id, provider, firstName, lastName, fullName, email, avatarUrl, authMethod, isAdmin, locale, hasher, password, salt
        FROM users
             """
        ).list(userParser)
    }
  }

  // store the preferred locale for the user with the given name
  def updateLocale(id: UserId, locale: String): DbUser = {
    DB.withConnection {
      implicit connection => {
        SQL(
          """
           UPDATE users
           SET locale = {locale}
           WHERE upper(id) = upper({id})
            and upper(provider) = upper({provider})
          """
        ).on(
          'locale -> locale,
          'id -> id.id,
          'provider -> id.providerId
        ).executeUpdate()

        UserService.find(id).get.asInstanceOf[DbUser]
      }
    }
  }

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
}