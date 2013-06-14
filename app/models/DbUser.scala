package models

import play.api.Play.current
import anorm._
import play.api.db.DB
import securesocial.core._
import securesocial.core.PasswordInfo
import securesocial.core.UserId
import securesocial.core.RequestWithUser
import securesocial.core.OAuth2Info
import securesocial.core.OAuth1Info
import play.api.mvc.AnyContent

case class DbUser(id: UserId, firstName: String, lastName: String, fullName: String, email: Option[String],
                  avatarUrl: Option[String], authMethod: AuthenticationMethod, isAdmin: Boolean, locale: String,
                  oAuth1Info: Option[OAuth1Info] = None, oAuth2Info: Option[OAuth2Info] = None, passwordInfo: Option[PasswordInfo] = None) extends Identity

case class DbUserAccountActivation(id: UserId, firstName: String, lastName: String, fullName: String, email: Option[String],
                                   avatarUrl: Option[String], authMethod: AuthenticationMethod, isAdmin: Boolean, locale: Option[String],
                                   oAuth1Info: Option[OAuth1Info] = None, oAuth2Info: Option[OAuth2Info] = None, passwordInfo: Option[PasswordInfo] = None)

object DbUser {
  def fromRequest(locale: Option[String] = None, isAdmin: Boolean = false)(implicit request: RequestWithUser[AnyContent]): Option[DbUser] = {
    request.user.map(_ match {
      case user: DbUser => user
      case SocialUser(id, firstName, lastName, fullName, email, avatar, authMethod, oauth1, oauth2, passInfo) => DbUser(id, firstName, lastName, fullName, email, avatar, authMethod, isAdmin, locale.getOrElse("EN"), oauth1, oauth2, passInfo)
    })
  }

  // store the preferred locale for the user with the given name
  def updateLocale(id: UserId, locale: String): DbUser = {
    DB.withConnection {
      implicit connection => {
        val res = SQL(
          """
           UPDATE users
           SET locale = {locale}
           WHERE upper(id) = upper({id})
            and upper(provider) = upper({provider})
          """
        ).on(
          'locale -> locale,
          'id -> id.id,
          'provider -> id.providerId
        ).executeUpdate()

        UserService.find(id).get.asInstanceOf
      }
    }
  }
}