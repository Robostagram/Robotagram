package models

import collection.mutable.HashMap
import concurrent.Lock

// in charge of handling the web sockets channels and rooms
object WsManager {
  private val lock: Lock = new Lock()

  // roomName -> Room
  private val _rooms : HashMap[String, WsRoom] = new HashMap[String, WsRoom]()
  // fill it from db names at the beginning
  DbRoom.findAll.foreach{dbRoom =>
    addRoom(dbRoom.name)
  }

  private def addRoom(roomName:String){
    lock.acquire()
    try{
      _rooms += ((roomName, new WsRoom(roomName)))
    }
    finally{
      lock.release()
    }
  }

  // get room by name
  def room(roomName: String): Option[WsRoom] = {
    _rooms.get(roomName)
  }

  // get the room of a player by name
  def roomForPlayer(playerName: String) : Option[WsRoom] = {
    _rooms.values.find{wsRoom =>
      wsRoom.hasPlayer(playerName)
    }
  }

  def notifyRoom(roomName:String, message: String){
    room(roomName).map(r=> r.sendAll(message))
  }

  def rooms : Iterable[(String, Iterable[String])] = {
    _rooms.values.map{r=>
      (r.name, r.players.keys)
    }
  }

}
