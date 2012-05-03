package models

import play.api.mvc.Request


class User(var nickname: String) {
}

object User {
  def fromRequest(implicit request: Request[Any]): User = {
    request.session.get("username") match {
      case Some(user) => new User(user)
      case None => AnonymousUser
    }
  }
}

object AnonymousUser extends User("unknown") {}
