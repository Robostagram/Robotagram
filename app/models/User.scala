package models

import play.api.mvc.Request
import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._


case class User(id: Pk[Long], name: String, email: String, password: String)

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
      get[String]("users.email") ~
      get[String]("users.password") map {
      case id~name~email~password => User(id, name, email, password)
    }
  }

  // -- Queries

  /**
   * Retrieve a User from name.
   */
  def findByName(name: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("""
        SELECT id, name, email, password
        FROM users
        WHERE upper(name) = upper({name})
        """
      ).on(
        'name -> name
      ).as(User.simple.singleOpt)
    }
  }

  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("""
          SELECT id, name, email, password
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
          select id, name, email, password
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
         SELECT id, name, email, password
         FROM users
         WHERE upper(name) = upper({name}) and password = {password}
        """
      ).on(
        'name -> name,
        'password -> password
      ).as(User.simple.singleOpt)
    }
  }

  /**
   * Create a User.
   *
   * return None if user cannot be created
   */
  def create(user: User): Option[Long] = {
    findByName(user.name) match {
      // check if a user does not exist already ( with same name / case-insensitive)
      case Some(user) => None // a user exists with that name , stop here !
      case _ =>
        DB.withConnection { implicit connection =>

        // Get the user id
          val id: Long = user.id.getOrElse {
            SQL("select nextval('users_seq')").as(scalar[Long].single)
          }

          // Insert the user
          SQL(
            """
           insert into users values (
             {id}, {name}, {email}, {password}
           )
            """
          ).on(
            'id -> id,
            'name -> user.name,
            'email -> user.email,
            'password -> user.password
          ).executeInsert()

          Some(id)

        }
    }

  }

}
