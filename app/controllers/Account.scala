package controllers


import play.api.mvc._
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models._
import play.api.data.validation.Constraints
import play.api.i18n.Messages


object Account extends CookieLang{

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
      case (name, _, _, _ , _) => !DbUser.findByName(name).isDefined
    })
      .verifying("An account already exists with that e-mail", fields => fields match {
      case (_, email, _, _ , _) => !DbUser.findByEmail(email).isDefined
    })
  )

  def register = Action{ implicit request =>
    Ok(views.html.account.register(accountCreationForm))
  }

  def createAccount = Action{ implicit request =>
    accountCreationForm.bindFromRequest.fold(
      failedPostedForm => Ok(views.html.account.register(failedPostedForm)),
      successForm => successForm match{
        case (name, email, _, password, _) => {
          DbUser.create(None, name, email, password).map {userId=>
            // TODO: send an email with the validation Url
            Redirect(routes.Account.accountCreated(name))
              .flashing("info" -> Messages("register.result.success"))
          }.getOrElse(
            // create returned "None" ... creation failed
            Ok(views.html.account.register(accountCreationForm.bindFromRequest()))
              .flashing("error" -> Messages("register.result.failure"))
          )
        }
      }
    )
  }

  def accountCreated(userName : String) = Action{ implicit request =>
     DbUser.findActivationByName(userName).map{ activationInfo =>
       Ok(views.html.account.accountCreationConfirmation(userName, routes.Account.activateAccount(userName, activationInfo.activationToken).url))
     }.getOrElse(NotAcceptable("unknown user ?!"))

  }

  def activateAccount(name : String, token : String) = Action { implicit request =>
    // look up the user by name and activation_token
    DbUser.findActivationByName(name).map{ activationInfo =>
      activationInfo.activatedOn match {
        // account is already activated
        case Some(d) => Ok(views.html.account.accountActivationFailure(name, Messages("activateAccount.result.failure.accountAlreadyActive")))
        // account is not activated yet
        case _ => {
          val result = DbUser.activate(name, token)
          if (result){
            Ok(views.html.account.accountActivationConfirmation(name, Messages("activateAccount.result.success")))
          }else{
            Ok(views.html.account.accountActivationFailure(name, Messages("activateAccount.result.failure")))
          }
        }
      }
    }.getOrElse(
      // unknown user !
      Ok(views.html.account.accountActivationFailure(name, Messages("activateAccount.result.failure.unknownUser")))
    )
  }
}
