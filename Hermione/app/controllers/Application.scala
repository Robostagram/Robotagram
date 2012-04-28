package controllers

import play.api._
import play.api.mvc._
import models.DefaultBoard

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("my application is ready."))
  }


  def board = Action {
    Ok(views.html.board(DefaultBoard))
  }
}
