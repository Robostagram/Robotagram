package models

import play.api.libs.iteratee.{Enumerator, PushEnumerator}

class WsPlayer(val name : String){

  val channel: PushEnumerator[String] = Enumerator.imperative[String]();

  def send(message: String) {
    channel.push(message)
  }

  def sayGoodBye(){
    channel.close()
  }
}
