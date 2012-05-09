package controllers

import play.api.mvc._
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models.{AnonymousUser, User}
import play.api.data.validation.Constraints

object Authentication extends Controller {

  val loginForm = Form("nickname" -> text.verifying(Constraints.nonEmpty)
                                          .verifying(Constraints.maxLength(40))
                                          .verifying(Constraints.minLength(4))
                                          .verifying(Constraints.pattern("""^([A-Za-z]|[0-9]|_)*$""".r, "Letters, numbers and _", "Must contain only non-accentuated letters, numbers or underscores")))

  def authenticate(redirectTo: Option[String] = None) = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        failedPostedForm => Ok(views.html.login(failedPostedForm, redirectTo)),       // redisplay the page with posted form to show errors
        postedNickname => {
          var destinationUrl = ""
          redirectTo match {
            case Some(redirection) => destinationUrl = redirection
            case _ => destinationUrl = routes.Home.index().absoluteURL()  // default to home
          }
          Redirect(destinationUrl)
            .withSession("username" -> postedNickname)
            .flashing("success" -> "You are now logged in")
        }
      )
  }

  // login + redirect url after login (optional, defaults to home page)
  def login(redirectTo: Option[String] = None) = Action {
    implicit request =>
      Ok(views.html.login(loginForm, redirectTo))
  }

  def logout = Action {
    implicit request =>
      Application.playerDisconnected(User.fromRequest.nickname);
      Redirect(routes.Home.index())
        .withNewSession
        .flashing("success" -> "You have been logged out"
      )
  }

  object Secured {
    // Authentication check: executes the wrapped action only if a username is found in the session
    def Authenticated[A](action: Action[A]): Action[A] = Action(action.parser) {
      implicit request => {
        User.fromRequest match {
          case AnonymousUser => Redirect(routes.Authentication.login(Some(request.uri)))
                                    .flashing("info" -> "You must be authentified to access the page you requested." )
          case _ => action(request)
        }
      }
    }
  }

}

