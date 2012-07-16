package models

import play.api.Play.current
import anorm._
import anorm.SqlParser._
import anorm.~
import play.api.db.DB
import scala.Some

case class DbBoard(id: Pk[Long], name: String, data: String)

object DbBoard{

  // -- Parsers

  /**
   * Parse a DbBoard from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("boards.id") ~
      get[String]("boards.name") ~
      get[String]("boards.data") map {
      case id~name~data => DbBoard(id, name, data)
    }
  }


  // -- Queries

  /**
   * Retrieve a Board from Id.
   */
  def findById(id: Long): Option[models.DbBoard] = {
    DB.withConnection { implicit connection =>
      SQL("""
        SELECT id, name, data
        FROM boards
        WHERE id ={id}
          """
      ).on(
        'id -> id
      ).as(DbBoard.simple.singleOpt)
    }
  }

  /**
   * Retrieve a Board from name.
   */
  def findByName(name: String): Option[models.DbBoard] = {
    DB.withConnection { implicit connection =>
      SQL("""
        SELECT id, name, data
        FROM boards
        WHERE upper(name) = upper({name})
          """
      ).on(
        'name -> name
      ).as(DbBoard.simple.singleOpt)
    }
  }


  /**
   * Retrieve all boards.
   */
  def findAll: Seq[DbBoard] = {
    DB.withConnection { implicit connection =>
      SQL("""
          select id, name, data
          from boards
          """
      ).as(DbBoard.simple *)
    }
  }


  /**
   * Create a Board in the db.
   *
   * return None if board cannot be created
   *
   */
  def create(id:Option[Long], name:String, data:String): Option[Long] = {
    findByName(name) match {
      // check if a board does not exist already ( with same name / case-insensitive)
      case Some(_) => None // a board exists with that name , stop here !
      case _ =>
        DB.withConnection { implicit connection =>

        // Get the board id (from db, or from arguments)
          val theId: Long = id.getOrElse {
            SQL("select nextval('boards_seq')").as(scalar[Long].single)
          }

          // Insert the board
          SQL(
            """
         insert into boards (
           id, name, data
         )
         values (
           {id}, {name}, {data}
         )
            """
          ).on(
            'id -> theId,
            'name -> name,
            'data -> data
          ).executeInsert()

          Some(theId)
        }
    }
  }

}
