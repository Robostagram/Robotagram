package controllers

import play.api._
import play.api.mvc._
import util.Random
import models._


object Application extends Controller {

  def index = Action {
    Ok(views.html.index("my application is ready."))
  }


  def newGame = Action {
    Ok(views.html.board(Game.randomGame()))
  }
}
