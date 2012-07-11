package controllers

import play.api.mvc._
import models._


object Home extends Controller {

  def index = Action {
    implicit request => {
      val user = User.fromRequest(request)
      Ok(views.html.home.index(user, DbRoom.findAll))
    }
  }

  def adminIndex = Action {
    implicit request => {
      val user = User.fromRequest(request)
      Ok(views.html.home.adminIndex(user))
    }
  }

}
