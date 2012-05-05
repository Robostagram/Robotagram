package controllers

import play.api.mvc.{Action, Controller}
import models.User


object Home extends Controller {

  def index = Action {
    implicit request => {
      val user = User.fromRequest(request)
      Ok(views.html.index(user))
    }
  }

}
