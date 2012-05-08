package controllers

import play.api.mvc._
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models.{AnonymousUser, User}

object Authentication extends Controller {

  val loginForm = Form("nickname" -> nonEmptyText)

  def authenticate(redirectTo: Option[String] = None) = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        failedForm => {
          // redisplay the page with posted form to show errors
          Ok(views.html.login(failedForm, redirectTo))},
        userFound => {
          redirectTo match {
            case Some(redirection) => Redirect(redirection).withSession("username" -> userFound).flashing(
              "success" -> "You are now logged in"
            )
            case _ => Redirect(routes.Home.index()).withSession("username" -> userFound).flashing(
              "success" -> "You are now logged in"
            )
          }
        }
      )
  }

  // login + redirect url after login (optionnal, defaults to home page)
  def login(redirectTo: Option[String] = None) = Action {
    implicit request =>
      Ok(views.html.login(loginForm, redirectTo))
  }

  def logout = Action {
    implicit request =>
      Application.playerDisconnected(User.fromRequest.nickname);
      Redirect(routes.Home.index()).withNewSession.flashing(
        "success" -> "You have been logged out"
      )
  }

  object Secured {
    // Authentication check: executes the wrapped action only if a username is found in the session
    def Authenticated[A](action: Action[A]): Action[A] = Action(action.parser) {
      implicit request => {
        val u = User.fromRequest
        u match {
          case AnonymousUser => Redirect(routes.Authentication.login(Some(request.uri)))
          case _ => action(request)
        }
      }
    }
  }

}

