package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.Logger
import play.api.mvc._
import models._

object Localise extends CookieLang{

  val localeForm = Form("locale" -> nonEmptyText)

  def changeLocale = Action { implicit request =>
    val referrer = request.headers.get(REFERER).getOrElse(HOME_URL)
    localeForm.bindFromRequest.fold(
      errors => {
        Logger.logger.debug("The locale can not be change to : " + errors.get)
        BadRequest(referrer)
      },
      locale => {
        User.fromRequest.map { user =>
          updateUserLang(user.name, locale)
        }.getOrElse{
          // naught
        }
        Logger.logger.debug("Changing locale to " + locale)
        Redirect(referrer).withCookies(Cookie(LANG, locale))
      }
    )
  }
  
  private def updateUserLang(username: String, locale: String) {
    DbUser.findByName(username).map{ dbUser =>
      if (dbUser.locale != Some(locale)) {
        Logger.logger.debug("Switching user " + username + " from locale " + dbUser.locale + " to " + locale)
        dbUser.updateLang(locale)
      }
    }.getOrElse{
      Logger.logger.debug("Wrong username provided for locale update in persistence, username: " + username)
    }
  }
}