package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.Logger
import models._
import securesocial.core.{UserService, UserId}
import LocaleAwareSecureSocial._

object Localise extends LocaleAwareSecureSocial {

  val localeForm = Form("locale" -> nonEmptyText)

  def changeLocale = UserAwareAction {
    implicit request =>
      val referrer = request.headers.get(REFERER).getOrElse(HOME_URL)
      localeForm.bindFromRequest.fold(
        errors => {
          Logger.logger.debug("The locale can not be changed to : " + errors.get)
          BadRequest(referrer)
        },
        locale => {
          DbUser.fromRequest().map {
            user =>
              updateUserLang(user.id, locale)
          }.getOrElse {
            // naught
          }
          Logger.logger.debug("Changing locale to " + locale)
          Redirect(referrer).withSession(request.request.session +(LANG, locale))
        }
      )
  }

  private def updateUserLang(id: UserId, locale: String) {
    UserService.find(id).map(_.asInstanceOf[DbUser]).map {
      dbUser =>
        if (dbUser.locale != Some(locale)) {
          Logger.logger.debug("Switching user " + id + " from locale " + dbUser.locale + " to " + locale)
          DbUser.updateLocale(id, locale)
        }
    }.getOrElse {
      Logger.logger.debug("Wrong user provided for locale update in persistence, id: " + id)
    }
  }
}