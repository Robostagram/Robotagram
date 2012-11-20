package controllers

import play.api.mvc.{Action, Controller, Cookie, RequestHeader}
import play.api.i18n.Lang
import play.Play

trait CookieLang extends Controller {

  override implicit def lang(implicit request: RequestHeader) = {
    request.cookies.get(LANG) match {
      case None => super.lang(request)
      case Some(cookie) => Lang(cookie.value)
    }
  }
  
  protected val LANG = "lang"
  protected val HOME_URL = "/"
}

object CookieLang {
  // ugly but Lang.availables from Play java API is not accessible in Scala...
  val AVAILABLE_LANGS = Play.application().configuration().getString("application.langs").split(",")
}