package controllers

import play.api.db.DB
import play.api.Play.current
import anorm._
import models._
import helpers.AdminOnly
import securesocial.core.{PasswordInfo, AuthenticationMethod, UserService, UserId}
import scala.Some
import org.mindrot.jbcrypt.BCrypt

object Admin extends LocaleAwareSecureSocial {

  def listUser = SecuredAction(AdminOnly) {
    implicit request => Ok(views.html.users.list(DbUser.findAll, DbUser.find(request.user.id)))
  }

  def reset = SecuredAction(AdminOnly) {
    implicit request => {
      cleanAll
      bootstrap
      Ok(views.html.home.adminIndex(DbUser.find(request.user.id)))
    }
  }

  // order matters, because of id references between tables
  private def cleanAll() {
    for (table <- Seq("scores", "games", "rooms", "boards", "users")) removeAllFrom(table)
  }

  /**
   * Remove all entries from a table.
   */
  private def removeAllFrom(table: String) = {
    DB.withConnection {
      implicit connection =>
        SQL("delete from " + table).execute()
    }
  }

  def bootstrap = {
    // populate users with some known names ;)
    if (DbUser.findAll.isEmpty) {
      val salt = BCrypt.gensalt()
      val hashedPwd = BCrypt.hashpw("hermione", salt)
      Seq(
        ("hermione", "hermione@robotagram.com"),
        ("kus", "kus@robotagram.com"),
        ("rom1", "rom1@robotagram.com"),
        ("bzn", "bzn@robotagram.com"),
        ("mithfindel", "mithfindel@robotagram.com"),
        ("tibal", "tibal@robotagram.com"),
        ("nire", "nire@robotagram.com")
      ).foreach(tup => tup match {
        case (name, email) => UserService.save(DbUser(UserId(name, "robotagram"), "", "", name, Some(email), None, AuthenticationMethod.UserPassword, true, "fr", None, None, Some(PasswordInfo("bcrypt", hashedPwd, Some(salt)))))
      })
    }

    // populate the default boards (take the standard board and then make variations ...
    if (DbBoard.findAll.isEmpty) {
      val standardBoard = Board.boardFromFile("app/resources/Standard.board", 0, "no name")
      val QUARTER_COMBOS = Array((1, 2, 3), (1, 3, 2), (2, 1, 3), (2, 3, 1), (3, 1, 2), (3, 2, 1))
      QUARTER_COMBOS.zipWithIndex.foreach(tup => tup match {
        case (combination, index) =>
          val boardId = (index + 1).asInstanceOf[Long] //start at one
        val boardName = "Standard.board (" + combination._1 + ", " + combination._2 + ", " + combination._3 + ")"
          val boardData = Board.boardToString(standardBoard.transformQuarters(combination))
          DbBoard.create(Some(boardId), boardName, boardData)
      })
    }

    // create the 2 default rooms ...
    if (DbRoom.findAll.isEmpty) {
      DbRoom.create(Some(1), "default")
      DbRoom.create(Some(2), "default2")
    }

    /*DbGame.getActiveInRoomOrCreate("default", () => {
      var idOfBoardToLoad = new Random().nextInt(6) + 1
      var b = Board.loadById(idOfBoardToLoad).get
      var g = Goal.randomGoal()
      var robots = Board.randomRobots(b)
      DbGame.prepareGameToStore(1, Game.DEFAULT_GAME_DURATION,b, g, robots)
    }) */

  }
}
