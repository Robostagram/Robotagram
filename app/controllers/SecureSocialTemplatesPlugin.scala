package controllers

import play.api.mvc.{RequestHeader, Request}
import play.api.data.Form
import play.api.templates.{Txt, Html}
import securesocial.controllers.Registration.RegistrationInfo

class SecureSocialTemplatesPlugin(application: play.api.Application) extends securesocial.controllers.DefaultTemplatesPlugin(application) with CookieLang {

  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)],
                               msg: Option[String] = None): Html = {
    views.html.authentication.login(form, msg)(request.flash, request, lang)
  }

  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
    views.html.authentication.startSignUp(form)(request.flash, request, lang)
  }

  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
    views.html.authentication.signUp(form, token)(request.flash, request, lang)
  }
}
