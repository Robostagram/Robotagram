package models

import scala._
import scala.collection.immutable.HashSet

class Room(val id: String) {
  var players:Set[Player] = new HashSet[Player]
  var game: Game = null

  def withPlayer(name:String):Player = {
    val option:Option[Player] = players.find(player => player.name == name)
    if (option == None) {
      val player:Player = new Player(name)
      players += player
      return player
    }
    val player = option.get
    player
  }

  def withoutPlayer(name: String): Player = {
    val option: Option[Player] = players.find(player => player.name == name)
    if (option == None) {
      return null
    }
    val player: Player = option.get
    players -= player
    player
  }
}