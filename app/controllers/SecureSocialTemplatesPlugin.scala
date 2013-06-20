package controllers

import play.api.mvc.Request
import play.api.data.Form
import play.api.templates.Html
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.core.SecuredRequest
import securesocial.controllers.PasswordChange.ChangeInfo

class SecureSocialTemplatesPlugin(application: play.api.Application) extends securesocial.controllers.DefaultTemplatesPlugin(application) with LocaleAwareSecureSocial {

  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)],
                               msg: Option[String] = None): Html = {
    views.html.authentication.login(form, msg)(flash, request, lang)
  }

  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
    views.html.authentication.startSignUp(form)(flash, request, lang)
  }

  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
    views.html.authentication.signUp(form, token)(flash, request, lang)
  }

  override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = {
    views.html.authentication.startResetPassword(form)(flash, request, lang)
  }

  override def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = {
    views.html.authentication.resetPassword(form, token)(flash, request, lang)
  }

  override def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]): Html = {
    views.html.authentication.changePassword(form)(flash, request, lang)
  }

  override def getNotAuthorizedPage[A](implicit request: Request[A]): Html = {
    views.html.authentication.notAuthorized()(flash, request, lang)
  }
}
