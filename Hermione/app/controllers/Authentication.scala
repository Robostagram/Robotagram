package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._

object Authentication extends Controller {

  val loginForm = Form("nickname" -> nonEmptyText)

  def authenticate(redirectTo: String = null) = Action {implicit request =>
    loginForm.bindFromRequest.fold(
      noUserError => Redirect(routes.Home.index()),
      userFound =>{
        // no very scala-ish :-/
        if (redirectTo == null ) { //no redirect url -> go back home - and only redirect to local urls starting with '/'
          Redirect(routes.Home.index()).withSession("username" -> userFound)
        }
        else{ // redirectUrl -> go there
          Redirect(redirectTo).withSession("username" -> userFound)
        }
      }
    )
  }

  // login + redirect url after login (optionnal, defaults to home page)
  def login(redirectTo: String = null) = Action {
    Ok(views.html.login(loginForm, redirectTo))
  }

  def logout = Action {
    Ok("ta mere en slip").withNewSession
  }

  object Secured {
    // Authentication check: executes the wrapped action only if a username is found in the session
    def Authenticated[A](action: Action[A]): Action[A] = Action(action.parser) {
      request =>
        request.session.get("username") match {
          case Some(user) => action(request)
          case None => Redirect(routes.Authentication.login(request.uri))
        }
    }
  }
}

