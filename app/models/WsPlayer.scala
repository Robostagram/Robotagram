package models

import play.api.libs.iteratee.Concurrent

class WsPlayer(val name: String){

  val (enum, channel) = Concurrent.broadcast[String]

  def send(message: String) {
    channel.push(message)
  }

  def sayGoodBye(){
    channel.end()
  }
}
