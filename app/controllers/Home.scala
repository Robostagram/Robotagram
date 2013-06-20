package controllers

import models._

object Home extends LocaleAwareSecureSocial {

  def index = UserAwareAction {
    implicit request =>
      val roomsAndParticipants = WsManager.rooms.map(t => (t._1, t._2.size)).toSeq
      Ok(views.html.home.index(DbUser.fromRequest(), roomsAndParticipants))
  }

  def adminIndex = UserAwareAction {
    implicit request => {
      Ok(views.html.home.adminIndex(DbUser.fromRequest()))
    }
  }
}
