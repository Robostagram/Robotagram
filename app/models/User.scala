package models

import play.api.mvc.Request

case class User(id: Long, name: String, email: String, isAdmin: Boolean, locale: Option[String])

object User {
  def fromRequest(implicit request: Request[Any]): Option[User] = {
    request.session.get("username").map{userName =>
    // we have an email in session ... look it up ...
      DbUser.findByName(userName).map{theUser =>
        new User(theUser.id.get, theUser.name, theUser.email, theUser.isAdmin, theUser.locale)
      } // name does not match a known user
    }.getOrElse(None) // no name provided
  }

}

