package services

import securesocial.core._
import play.api.mvc.{RequestHeader, Session}
import securesocial.core.LoginEvent
import play.api.Application
import controllers.LocaleAwareSecureSocial.LANG
import models.DbUser

class SocialEventsListener(application: Application) extends EventListener {
  override def id: String = "socialEventListener"

  def onEvent(event: Event, request: RequestHeader, session: Session): Option[Session] = event match {
    case e: LoginEvent => Some(withUserLocale(e.user, session))
    case e: SignUpEvent => Some(session)
    case _ => None
    //      case e: LogoutEvent => "logout"
    //      case e: PasswordResetEvent => "password reset"
    //      case e: PasswordChangeEvent => "password change"
  }

  private def withUserLocale(user: Identity, session: Session): Session = {
    session + (LANG -> user.asInstanceOf[DbUser].locale)
  }
}
