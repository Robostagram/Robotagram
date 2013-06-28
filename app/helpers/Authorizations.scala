package helpers

import securesocial.core.{UserService, Identity, Authorization}
import models.DbUser

object AdminOnly extends Authorization {
  def isAuthorized(user: Identity): Boolean = {
    UserService.find(user.id).exists(_.asInstanceOf[DbUser].isAdmin)
  }
}

object Authenticated extends Authorization {
  def isAuthorized(user: Identity): Boolean = {
    UserService.find(user.id).isDefined
  }
}
