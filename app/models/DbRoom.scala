package models

import play.api.Play.current
import scala._
import anorm._
import anorm.SqlParser._
import play.api.db.DB
import anorm.~
import scala.Some

case class DbRoom(id: Pk[Long], name: String)

object DbRoom{

  // -- Parsers

  /**
   * Parse a DbBoard from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("rooms.id") ~
      get[String]("rooms.name") map {
      case id~name => DbRoom(id, name)
    }
  }


  // -- Queries

  /**
   * Retrieve a Room from Id.
   */
  def findById(id: Long): Option[models.DbRoom] = {
    DB.withConnection { implicit connection =>
      SQL("""
        SELECT id, name
        FROM rooms
        WHERE id ={id}
          """
      ).on(
        'id -> id
      ).as(DbRoom.simple.singleOpt)
    }
  }

  /**
   * Retrieve a Room from name.
   */
  def findByName(name: String): Option[models.DbRoom] = {
    DB.withConnection { implicit connection =>
      SQL("""
        SELECT id, name
        FROM rooms
        WHERE upper(name) = upper({name})
          """
      ).on(
        'name -> name
      ).as(DbRoom.simple.singleOpt)
    }
  }


  /**
   * Retrieve all boards.
   */
  def findAll: Seq[DbRoom] = {
    DB.withConnection { implicit connection =>
      SQL("""
          select id, name
          from rooms
          """
      ).as(DbRoom.simple *)
    }
  }


  /**
   * Create a Board in the db.
   *
   * return None if board cannot be created
   *
   */
  def create(id:Option[Long], name:String): Option[Long] = {
    findByName(name) match {
      // check if a board does not exist already ( with same name / case-insensitive)
      case Some(_) => None // a board exists with that name , stop here !
      case _ =>
        DB.withConnection { implicit connection =>

        // Get the board id (from db, or from arguments)
          val theId: Long = id.getOrElse {
            SQL("select nextval('rooms_seq')").as(scalar[Long].single)
          }

          // Insert the board
          SQL(
            """
         insert into rooms (
           id, name
         )
         values (
           {id}, {name}
         )
            """
          ).on(
            'id -> theId,
            'name -> name
          ).executeInsert()

          Some(theId)
        }
    }
  }

}
