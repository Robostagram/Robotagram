package models

import play.api.mvc.Request
import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import java.util.{UUID, Date}
import java.util


case class User(id: Pk[Long], name: String, email: String)

case class UserAccountActivation(id: Pk[Long], name: String, activationToken: String, activatedOn : Option[Date])

object User {

  def fromRequest(implicit request: Request[Any]): Option[User] = {
    request.session.get("username").map{userName =>
      // we have an email in session ... look it up ...
      User.findByName(userName).map{theUser =>
        theUser
      } // name does not match a known user
    }.getOrElse(None) // no name provided
  }

  // -- Parsers

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
      get[Pk[Long]]("users.id") ~
      get[String]("users.name") ~
      get[String]("users.email") map {
      case id~name~email => User(id, name, email)
    }
  }

  /**
   * Parse the validation information from a resultset
   */
  val activationInfo = {
      get[Pk[Long]]("users.id") ~
      get[String]("users.name") ~
      get[String]("users.activation_token") ~
      get[Option[Date]]("users.activated_on") map {
      case id~name~token~activationDate => UserAccountActivation(id, name, token,activationDate)
    }
  }

  // -- Queries

  /**
   * Retrieve a User from name.
   */
  def findByName(name: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("""
        SELECT id, name, email
        FROM users
        WHERE upper(name) = upper({name})
        """
      ).on(
        'name -> name
      ).as(User.simple.singleOpt)
    }
  }

  /**
   * Retrieve account activation information by name
   */
  def findActivationByName(name:String) : Option[UserAccountActivation] = {
    DB.withConnection { implicit connection =>
      SQL("""
        SELECT id, name, activation_token, activated_on
        FROM users
        WHERE upper(name) = upper({name})
          """
      ).on(
        'name -> name
      ).as(User.activationInfo.singleOpt)
    }
  }


  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("""
          SELECT id, name, email
          FROM users
          WHERE upper(email) = upper({email})
          """
      ).on(
        'email -> email
      ).as(User.simple.singleOpt)
    }
  }

  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
    DB.withConnection { implicit connection =>
      SQL("""
          select id, name, email
          from users
          """
      ).as(User.simple *)
    }
  }

  /**
   * Authenticate a User.
   */
  def authenticate(name: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         SELECT id, name, email
         FROM users
         WHERE upper(name) = upper({name})
          and password = {password}
          and activated_on IS NOT NULL
        """ //list only active user accounts
      ).on(
        'name -> name,
        'password -> password
      ).as(User.simple.singleOpt)
    }
  }

  // activate an account with a given name and a given token
  // returns true if the account was activated
  // returns false if the account was not activated (wrong username, wrong token or account already active)
  def activate(userName: String, activationToken: String): Boolean = {
    DB.withConnection { implicit connection =>
      val res = SQL(
        """
           UPDATE users
           SET activated_on = {now}
           WHERE upper(name) = upper({name})
            and activation_token = {token}
            and activated_on IS NULL
        """
      ).on(
        'now -> new Date(),
        'name -> userName,
        'token -> activationToken
      ).executeUpdate()

      res > 0 // true if modified something . false if not
    }
  }

  /**
   * Create a User.
   *
   * return None if user cannot be created
   *
   * the activated_on should be passed only to create accounts that are already validated
   */
  def create(id:Option[Long], name:String, email:String, password:String, activated_on : Option[Date] = None): Option[Long] = {
    findByName(name) match {
      // check if a user does not exist already ( with same name / case-insensitive)
      case Some(user) => None // a user exists with that name , stop here !
      case _ =>
        DB.withConnection { implicit connection =>

        // Get the user id
          val theId: Long = id.getOrElse {
            SQL("select nextval('users_seq')").as(scalar[Long].single)
          }

          // Insert the user
          SQL(
            """
           insert into users (
             id, name, email, password, created_on, activation_token, activated_on
           )
           values (
             {id}, {name}, {email}, {password}, {created_on}, {activation_token}, {activated_on}
           )
            """
          ).on(
            'id -> theId,
            'name -> name,
            'email -> email,
            'password -> password,
            'created_on -> new Date(),
            'activation_token -> UUID.randomUUID().toString,
            'activated_on -> activated_on
          ).executeInsert()

          Some(theId)

        }
    }

  }

}
