package services

import play.api.Application
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.UserId
import models.DbUser

class UserService(application: Application) extends UserServicePlugin(application) {
  //private var users = Map[String, Identity]()
  //private var tokens = Map[String, Token]()

  def find(id: UserId): Option[Identity] = {
    DbUser.findById(id)
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    DbUser.findByEmailAndProvider(email, providerId)
  }

  def save(user: Identity): Identity = {
    DbUser.save(user)
  }

  def save(token: Token) {
    //tokens += (token.uuid -> token)
  }

  def findToken(token: String): Option[Token] = {
    //tokens.get(token)
    None
  }

  def deleteToken(uuid: String) {
    //tokens -= uuid
  }

  def deleteTokens() {
    //tokens = Map()
  }

  def deleteExpiredTokens() {
    //tokens = tokens.filter(!_._2.isExpired)
  }
}
