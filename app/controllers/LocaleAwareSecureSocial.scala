package controllers

import securesocial.core.SecureSocial
import play.api.i18n.Lang
import play.Play
import play.api.mvc._
import scala.Some
import LocaleAwareSecureSocial.LANG

trait LocaleAwareSecureSocial extends SecureSocial {
  override implicit def lang(implicit request: RequestHeader) = {
    request.session.get(LANG) match {
      case None => super.lang(request)
      case Some(cookie) => Lang(cookie)
    }
  }
}

object LocaleAwareSecureSocial {
  // ugly but Lang.availables from Play java API is not accessible in Scala...
  val AVAILABLE_LANGS = Play.application().configuration().getString("application.langs").split(",")
  val LANG = "lang"
  val HOME_URL = "/"
}
