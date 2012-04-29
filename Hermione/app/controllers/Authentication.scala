package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._

class Authentication extends Controller {

  val loginForm = Form("nickname" -> nonEmptyText)

  val redirectToIndex = Redirect(routes.Application.newGame(0))

  def authenticate = Action {implicit request =>
    loginForm.bindFromRequest.fold(
      noUserError => redirectToIndex,
      userFound => redirectToIndex.withSession("username" -> userFound)
    )
  }

  def login = Action {
    Ok(views.html.login(loginForm))
  }

  def logout = Action {
    Ok("ta mere en slip").withNewSession
  }
}

object Authentication extends Authentication {

  object Secured {
    // Authentication check: executes the wrapped action only if a username is found in the session
    def Authenticated[A](action: Action[A]): Action[A] = Action(action.parser) {
      request =>
        request.session.get("username") match {
          case Some(user) => action(request)
          case None => Redirect(routes.Authentication.login)
        }
    }
  }
}

