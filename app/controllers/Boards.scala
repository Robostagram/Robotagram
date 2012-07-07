package controllers

import play.api.mvc._
import play.api.mvc.Controller

import models.DbBoard


// stupid controller just to test what we have in db ... and display it
object Boards extends Controller{

  def index = Action { implicit request =>
    Ok(views.html.boardList(DbBoard.findAll))
  }

}
