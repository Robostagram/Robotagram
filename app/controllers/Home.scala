package controllers

import play.api.mvc._
import models._
import controllers.Authentication.Secured

object Home extends CookieLang {

  def index = Action { implicit request =>
    val user = User.fromRequest(request)
    val roomsAndParticipants = WsManager.rooms.map(t => (t._1, t._2.size)).toSeq
    Ok(views.html.home.index(user, roomsAndParticipants))
  }

  def adminIndex = Secured.AdminAuthenticated {
    Action {
      implicit request => {
        val user = User.fromRequest(request)
        Ok(views.html.home.adminIndex(user))
      }
    }
  }
}
