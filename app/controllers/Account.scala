package controllers


import play.api.mvc._
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models.User
import play.api.data.validation.Constraints
import play.api.i18n.Messages
import anorm.NotAssigned


object Account extends Controller{

  var accountCreationForm = Form(
    tuple(
      "name" -> text.verifying(Constraints.nonEmpty, Constraints.maxLength(40), Constraints.minLength(2))
        .verifying(Constraints.pattern("""^([A-Za-z]|[0-9]|_)*$""".r, "Letters, numbers and _", "Must contain only non-accentuated letters, numbers or underscores")),
      "email1" -> email,
      "email2" -> email,
      "password1" -> text.verifying(Constraints.nonEmpty, Constraints.maxLength((40)), Constraints.minLength(5)),
      "password2" -> text.verifying(Constraints.nonEmpty)
    )
      .verifying("The e-mail addresses are different", fields => fields match {
      case (_, email1, email2, _ , _) => email1.equals(email2)
    })
      .verifying("The passwords are different", fields => fields match {
      case (_, _, _, pw1, pw2) => pw1.equals(pw2)
    })
      .verifying("Username is already taken", fields => fields match {
      case (name, _, _, _ , _) => !User.findByName(name).isDefined
    })
      .verifying("An account already exists with that e-mail", fields => fields match {
      case (_, email, _, _ , _) => !User.findByEmail(email).isDefined
    })
  )

  def register = Action{ implicit request =>
    Ok(views.html.register(accountCreationForm))
  }

  def createAccount = Action{ implicit request =>
    accountCreationForm.bindFromRequest.fold(
      failedPostedForm => Ok(views.html.register(failedPostedForm)),
      successForm => successForm match{
        case (name, email, _, password, _) => {
          //TODO: try and create the user ...
          User.create(new User(NotAssigned, name, email, password)).map {userId=>
            Redirect(routes.Account.accountCreated(name))
              .flashing("info" -> Messages("register.result.success"))
          }.getOrElse(
            // create returned "None" ... creation failed
            Ok(views.html.register(accountCreationForm.bindFromRequest()))
              .flashing("error" -> Messages("register.result.failure"))
          )
        }
      }
    )
  }

  def accountCreated(userName : String) = Action{ implicit request =>
     Ok(views.html.accountCreationConfirmation(userName))
  }


}
