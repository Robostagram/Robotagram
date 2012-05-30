package models

import play.api.mvc.Request
import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._


case class User(id: Pk[Long], name: String, password: String)

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
      get[String]("users.password") map {
      case id~name~password => User(id, name, password)
    }
  }

  // -- Queries

  /**
   * Retrieve a User from name.
   */
  def findByName(name: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from users where name = {name}").on(
        'name -> name
      ).as(User.simple.singleOpt)
    }
  }

  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from users").as(User.simple *)
    }
  }

  /**
   * Authenticate a User.
   */
  def authenticate(name: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         select * from users where
         name = {name} and password = {password}
        """
      ).on(
        'name -> name,
        'password -> password
      ).as(User.simple.singleOpt)
    }
  }

  /**
   * Create a User.
   */
  def create(user: User): User = {
    DB.withConnection { implicit connection =>

    // Get the project id
      val id: Long = user.id.getOrElse {
        SQL("select next value for users_seq").as(scalar[Long].single)
      }

      // Insert the project
      SQL(
        """
           insert into users values (
             {id}, {name}, {password}
           )
        """
      ).on(
        'id -> id,
        'name -> user.name,
        'password -> user.password
      ).executeUpdate()

      user

    }
  }

}
