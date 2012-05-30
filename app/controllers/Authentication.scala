package controllers

import play.api.mvc._
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models.User
import play.api.data.validation.Constraints
import play.api.i18n.Messages

object Authentication extends Controller {

  val loginForm = Form(
                    tuple(
                      "nickname" -> text.verifying(Constraints.nonEmpty)
                                          .verifying(Constraints.maxLength(40))
                                          .verifying(Constraints.minLength(2))
                                          .verifying(Constraints.pattern("""^([A-Za-z]|[0-9]|_)*$""".r, "Letters, numbers and _", "Must contain only non-accentuated letters, numbers or underscores")),
                      "password" -> text.verifying(Constraints.nonEmpty)
                    )verifying("Invalid user name or password", fields => fields match {
                      case (e, p) => User.authenticate(e,p).isDefined
                    }
                    )
  )

  def authenticate(redirectTo: Option[String] = None) = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        // forget the submitted password
        failedPostedForm =>  Ok(views.html.login(failedPostedForm.fill(failedPostedForm("nickname").value.getOrElse(""), ""), redirectTo)),       // redisplay the page with posted form to show errors
        successForm => successForm match {
          case (user, password) => {
          //should be a way of obtaining the tuple directly, right ?
        var destinationUrl = ""
        redirectTo match {
        case Some(redirection) => destinationUrl = redirection
        case _ => destinationUrl = routes.Home.index().absoluteURL()  // default to home
        }
        Redirect(destinationUrl)
        .withSession("username" -> user)
        .flashing("success" -> Messages("login.result.success"))
        }

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
      User.fromRequest.map { user=>
        Gaming.playerDisconnected(user.name);
      }
      Redirect(routes.Home.index())
        .withNewSession
        .flashing("success" -> Messages("logout.result.success"))
  }

  object Secured {
    // Authentication check: executes the wrapped action only if a username is found in the session
    def Authenticated[A](action: Action[A]): Action[A] = Action(action.parser) { implicit request =>
        User.fromRequest.map { user =>
          action(request)
        }.getOrElse(
          Redirect(routes.Authentication.login(Some(request.uri)))
                  .flashing("info" -> Messages("login.authenticationRequired"))
        )
    }
  }

}

