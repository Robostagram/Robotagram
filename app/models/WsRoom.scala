package models

import collection.mutable.HashMap
import concurrent.Lock


class WsRoom(val name: String ) {
  private val lock: Lock = new Lock()

  // playerName -> channel
  private var players : HashMap[String, WsPlayer] = new HashMap[String, WsPlayer]()

  def hasPlayer(playerName: String) : Boolean = {
    players.get(playerName).isDefined
  }

  def player(playerName:String) : Option[WsPlayer] = {
    players.get(playerName)
  }

  def join(playerName:String) : WsPlayer = {
    lock.acquire()
    try{
      var p = new WsPlayer(playerName)
      players += ((playerName, p))
      p
    }
    finally{
      lock.release()
    }
  }

  // when a client disconnects, remove it from here !
  def forgetPlayer(playerName : String) {
    lock.acquire()
    try{
      players -= playerName
    }
    finally{
      lock.release()
    }
  }

  // close "nicely" the connection of the user  and forget him/her
  def kickPlayer(playerName:String){
    player(playerName).map{p=>
      p.sayGoodBye()
    }
    forgetPlayer(playerName)
  }

  // send a message to all players in the room
  def sendAll(message: String, exceptPlayer: Option[String] = None){
    players.filter( t => t match {
      case (name, player) => ! exceptPlayer.getOrElse("").equalsIgnoreCase(name) //ignore user with name exceptPlayer
    }
    ).values.foreach(p=> p.send(message))
  }

}
