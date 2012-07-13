package models

import play.api.libs.iteratee.{Enumerator, PushEnumerator}
import play.api.libs.iteratee.Input.EOF

class WsPlayer(val name : String){

  val channel: PushEnumerator[String] = Enumerator.imperative[String]();

  def send(message: String) {
    channel.push(message)
  }

  def sayGoodBye(){
    channel.close()
  }
}
