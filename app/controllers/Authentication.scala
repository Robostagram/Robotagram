package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import play.api.data.validation.Constraints
import play.api.i18n.Messages
import play.api.i18n.Lang
import securesocial.core.SecureSocial

object Authentication extends CookieLang with SecureSocial {

//  val loginForm = Form(
//    tuple(
//      "nickname" -> text.verifying(Constraints.nonEmpty)
//        .verifying(Constraints.maxLength(40))
//        .verifying(Constraints.minLength(2))
//        .verifying(Constraints.pattern( """^([A-Za-z]|[0-9]|_)*$""".r, "Letters, numbers and _", "Must contain only non-accentuated letters, numbers or underscores")),
//      "password" -> text.verifying(Constraints.nonEmpty).verifying(Constraints.minLength(6))
//    ) verifying("Invalid user name or password", fields => fields match {
//      case (e, p) => DbUser.authenticate(e, p).isDefined
//    }
//      )
//  )
//
//  def authenticate(redirectTo: Option[String] = None) = Action {
//    implicit request =>
//      loginForm.bindFromRequest.fold(
//        // forget the submitted password
//        failedPostedForm => Ok(views.html.authentication.login(failedPostedForm.fill(failedPostedForm("nickname").value.getOrElse(""), ""), redirectTo)), // redisplay the page with posted form to show errors
//        successForm => {
//          var destinationUrl = ""
//          redirectTo match {
//            case Some(redirection) => destinationUrl = redirection
//            case _ => destinationUrl = routes.Home.index().absoluteURL() // default to home
//          }
//          val username = successForm._1
//          val dbUser = DbUser.findByName(username).get //cant get None here
//
//          // apply redirection with user set
//          def redir = Redirect(destinationUrl)
//            .withSession(session + ("username" -> username)) // store the current user name in the session (=encrypted cookie)
//
//          // if the user has a preferred locale, change the language cookie and use the new language for the flash message
//          def redirLang = dbUser.locale.map {
//            l =>
//              (redir.withCookies(Cookie(LANG, l)), Lang(l))
//          }.getOrElse {
//            (redir, lang)
//          }
//          redirLang._1.flashing("success" -> Messages("login.result.success")(redirLang._2))
//        }
//      )
//  }

  // login + redirect url after login (optional, defaults to home page)
//  def login(redirectTo: Option[String] = None) = Action {
//    implicit request => {
//      User.fromRequest.map {
//        user =>
//          Redirect(routes.Home.index())
//      }.getOrElse(
//        Ok(views.html.authentication.login(loginForm, redirectTo))
//      )
//    }
//  }
//
//  def logout = Action {
//    implicit request =>
//    /*User.fromRequest.map { user=>
//      Gaming.playerDisconnected(user.name);
//    }*/
//      Redirect(routes.Home.index())
//        .withNewSession
//        .flashing("success" -> Messages("logout.result.success"))
//  }
//
//  object Secured {
//    // Authentication check: executes the wrapped action only if a username is found in the session
//    def Authenticated[A](action: Action[A]): Action[A] = Action(action.parser) {
//      implicit request =>
//        User.fromRequest.map {
//          user =>
//            action(request)
//        }.getOrElse(
//          Redirect(routes.Authentication.login(Some(request.uri)))
//            .flashing("info" -> Messages("login.authenticationRequired"))
//        )
//    }
//
//    def AdminAuthenticated[A](action: Action[A]): Action[A] = Action(action.parser) {
//      implicit request =>
//        User.fromRequest.map {
//          user =>
//            if (user.isAdmin) action(request)
//            else Unauthorized(views.html.authentication.E503(Some(user)))
//        }.getOrElse(
//          Unauthorized(views.html.authentication.E503(None))
//        )
//    }
//  }

}

