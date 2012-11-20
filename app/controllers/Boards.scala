package controllers

import play.api.mvc._
import play.api.mvc.Controller

import models.{User, Board, DbBoard}
import controllers.Authentication.Secured


// stupid controller just to test what we have in db ... and display it
object Boards extends CookieLang{

  def index = Secured.AdminAuthenticated {
      Action { implicit request =>
        Ok(views.html.boards.boardList(DbBoard.findAll, User.fromRequest))
      }
  }

  def preview(id:Long) = Secured.AdminAuthenticated {
    Action { implicit request =>
      Board.loadById(id).map{board =>
        Ok(views.html.boards.previewBoard(board, User.fromRequest))
      }.getOrElse(NotFound)
    }
  }

}
