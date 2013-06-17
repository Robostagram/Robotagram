package controllers

import play.api.mvc.Request
import play.api.data.Form
import play.api.templates.Html

class SecureSocialTemplatesPlugin(application: play.api.Application) extends securesocial.controllers.DefaultTemplatesPlugin(application) with CookieLang {

  /**
   * Returns the html for the login page
   * @param request
   * @tparam A
   * @return
   */
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)],
                               msg: Option[String] = None): Html = {
    views.html.authentication.login(form, msg)(request.flash, request, lang)
  }
}
