package controllers

import play.api.mvc._
import play.api.mvc.Controller

import models.{User, Board, DbBoard}


// stupid controller just to test what we have in db ... and display it
object Boards extends Controller{

  def index = Action { implicit request =>
    Ok(views.html.boardList(DbBoard.findAll, User.fromRequest))
  }

  def preview(id:Long) = Action { implicit request =>
    Board.loadById(id).map{board =>
      Ok(views.html.previewBoard(board, User.fromRequest))
    }.getOrElse(NotFound)
  }

}
