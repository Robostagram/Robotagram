package models

import collection.mutable.HashMap
import play.api.libs.iteratee.{Enumerator, PushEnumerator}


class WsRoom(val name: String ) {
  // playerName -> channel
  private var players : HashMap[String, WsPlayer] = new HashMap[String, WsPlayer]()

  def hasPlayer(playerName: String) : Boolean = {
    players.get(playerName).isDefined
  }

  def player(playerName:String) : Option[WsPlayer] = {
    players.get(playerName)
  }

  // send a message to all players in the room
  def sendAll(message: String, exceptPlayer: Option[String] = None){
    players.filter( t => t match {
      case (name, player) => ! exceptPlayer.getOrElse("").equalsIgnoreCase(name) //ignore user with name exceptPlayer
    }
    ).values.foreach(p=> p.send(message))
  }

}

class WsPlayer(val name : String){

  private val channel: PushEnumerator[String] = Enumerator.imperative[String]();

  def send(message: String) {
    channel.push(message)
  }
}
