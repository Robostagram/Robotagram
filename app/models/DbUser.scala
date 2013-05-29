package models

import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.db.DB
import securesocial.core._
import securesocial.core.UserId
import anorm.~
import scala.Some

case class DbUser(id: UserId, firstName: String, lastName: String, fullName: String, email: Option[String],
                  avatarUrl: Option[String], authMethod: AuthenticationMethod, isAdmin: Boolean, locale: String,
                  oAuth1Info: Option[OAuth1Info] = None, oAuth2Info: Option[OAuth2Info] = None, passwordInfo: Option[PasswordInfo] = None) extends Identity {
  def updateLang(locale: String) {
    DbUser.updateLocale(id, locale)
  }
}

case class DbUserAccountActivation(id: UserId, firstName: String, lastName: String, fullName: String, email: Option[String],
                                   avatarUrl: Option[String], authMethod: AuthenticationMethod, isAdmin: Boolean, locale: Option[String],
                                   oAuth1Info: Option[OAuth1Info] = None, oAuth2Info: Option[OAuth2Info] = None, passwordInfo: Option[PasswordInfo] = None)

object DbUser {

  // -- Parsers

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
      get[Option[String]]("users.locale") map {
      case id ~ provider ~ firstName ~ lastName ~ fullName ~ email ~ avatarUrl ~ authMethod ~ isAdmin ~ locale => DbUser(UserId(id, provider), firstName, lastName, fullName, email, avatarUrl, AuthenticationMethod(authMethod), isAdmin, locale)
    }
  }

  // -- Queries

  def findById(id: UserId): Option[DbUser] = {
    DB.withConnection {
      implicit connection =>
        SQL( """
        SELECT id, provider, firstName, lastName, fullName, email, avatarUrl, authMethod, isAdmin, locale
        FROM users
        WHERE upper(id) = upper({id})
         and upper(provider) = upper({provider})
             """
        ).on(
          'id -> id.id,
          'provider -> id.providerId
        ).as(DbUser.simple.singleOpt)
    }
  }

  def findByEmailAndProvider(email: String, provider: String): Option[DbUser] = {
    DB.withConnection {
      implicit connection =>
        SQL( """
        SELECT id, provider, firstName, lastName, fullName, email, avatarUrl, authMethod, isAdmin, locale
        FROM users
        WHERE upper(email) = upper({email})
         and upper(provider) = upper({provider})
             """
        ).on(
          'email -> email,
          'provider -> provider
        ).as(DbUser.simple.singleOpt)
    }
  }


  /**
   * Authenticate a User.
   */
  def authenticate(name: String, password: String): Option[DbUser] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
         SELECT id, provider, firstName, lastName, fullName, email, avatarUrl, authMethod, isAdmin, locale
         FROM users
         WHERE upper(name) = upper({name})
          and password = {password}
          and activated_on IS NOT NULL
          """ //list only active user accounts
        ).on(
          'name -> name,
          'password -> password
        ).as(DbUser.simple.singleOpt)
    }
  }

  // store the preferred locale for the user with the given name 
  def updateLocale(id: UserId, locale: String): Boolean = {
    DB.withConnection {
      implicit connection =>
        val res = SQL(
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

        res > 0 // true if modified something . false if not
    }
  }

  def save(user: Identity): Identity = {
    findById(user.id) match {
      case Some => update(user)
      case None => create(user)
    }
    findById(user.id).get
  }

  /**
   * Create a User.
   *
   * return None if user cannot be created
   *
   * the activated_on should be passed only to create accounts that are already validated
   */
  private def create(user: Identity): Identity = {
    DB.withConnection {
      implicit connection =>
      // Insert the user
        SQL(
          """
           UPDATE users
            SET firstName = {firstName}, lastName = {lastName}, fullName = {fullName}, email =  {email}, avatarUrl = {avatarUrl}, authMethod =  {authMethod}
            WHERE upper(id) = upper({id})
             and upper(provider) = upper({provider})
          """
        ).on(
          'id -> user.id.id,
          'provider -> user.id.providerId,
          'firstName -> user.firstName,
          'lastName -> user.lastName,
          'fullName -> user.fullName,
          'email -> user.email.getOrElse(""),
          'avatarUrl -> user.avatarUrl.getOrElse(" /assets/images/symbols/robot.png"),
          'authMethod -> user.authMethod
        ).executeInsert()
    }

    findById(user.id).get
  }

  private def update(user: Identity): Identity = {
    DB.withConnection {
      implicit connection =>
      // Insert the user
        SQL(
          """
           insert into users (
             id, provider, firstName, lastName, fullName, email, avatarUrl, authMethod
           )
           values (
             {id}, {provider}, {firstName}, {lastName}, {fullName}, {email}, {avatarUrl}, {authMethod}
           )
          """
        ).on(
          'id -> user.id.id,
          'provider -> user.id.providerId,
          'firstName -> user.firstName,
          'lastName -> user.lastName,
          'fullName -> user.fullName,
          'email -> user.email.getOrElse(""),
          'avatarUrl -> user.avatarUrl.getOrElse(" /assets/images/symbols/robot.png"),
          'authMethod -> user.authMethod
        ).executeUpdate()
    }

    findById(user.id).get
  }

}