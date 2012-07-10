package models

import collection.mutable.HashMap

// in charge of handling the web sockets channels and rooms
object WsManager {

  // roomName -> Room
  private val rooms : HashMap[String, WsRoom] = new HashMap[String, WsRoom]()
  // fill it from db names at the beginning
  DbRoom.findAll.foreach{dbRoom =>
    addRoom(dbRoom.name)
  }

  private def addRoom(roomName:String){
    rooms += ((roomName, new WsRoom(roomName)))
  }

  // get room by name
  def room(roomName: String): Option[WsRoom] = {
    rooms.get(roomName)
  }

  // get the room of a player by name
  def roomForPlayer(playerName: String) : Option[WsRoom] = {
    rooms.values.find{wsRoom =>
      wsRoom.hasPlayer(playerName)
    }
  }

  def notifyRoom(roomName:String, message: String){
    room(roomName).map(r=> r.sendAll(message))
  }

}
