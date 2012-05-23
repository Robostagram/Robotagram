package controllers

import scala.collection.mutable.HashMap
import play.api.mvc._
import models._
import models.Direction._
import models.Color._
import concurrent.Lock
import play.api.libs.iteratee.Iteratee
import controllers.Authentication.Secured
import play.api.libs.json.Json.toJson
import play.api.data.Form
import play.api.libs.json.{JsString, JsUndefined, Json, JsValue}

object Gaming extends Controller {

  // handle a game per room, with as many rooms as can be named (for now)
  var rooms: HashMap[String, Room] = new HashMap[String, Room]() // [roomId -> Room]
  rooms += (("default", new Room("default")))
  rooms += (("default2", new Room("default2")))
  
  // store where the users are
  var roomsByPlayer: HashMap[String, String] = new HashMap[String, String]() // [username -> roomId]
  val lock: Lock = new Lock()


  private def initializeGameIfNecessary(room: Room, playerName:String) {
    lock.acquire();
    roomsByPlayer -= playerName
    roomsByPlayer += ((playerName, room.id))
    var game = room.game
    try {
      var createNewGame: Boolean = game == null || game.isDone
      if (createNewGame) {
        room.players.foreach(player => player.channel.close())
        room.game = Game.randomGame()
      } else {  // if game already existed, notify other users of new user
        notifySummary(room, playerName)
      }
    } catch {
      case e => InternalServerError("WTF? " + e)
    } finally {
      lock.release()
    }
  }

  private def getRoom(roomId: String): Room = {
    val roomOpt = rooms.get(roomId)
    if (roomOpt == None) {
      NotFound("404 de la mort : room '" + roomId + "' does not exist. Only the room 'default' exists so far")
    }
    roomOpt.get
  }
  
  
  //
  // GET /rooms/n/games/current
  //
  def currentGame(roomId: String) = Secured.Authenticated {
    Action {
      implicit request =>
        val room = getRoom(roomId)
        val user = User.fromRequest(request)
        initializeGameIfNecessary(room, user.nickname)

        Redirect(routes.Gaming.getGame(room.id, room.game.uuid))
    }
  }

  //
  // GET /rooms/n/games/xx-xx-x-x-x-xxx
  //
  def getGame(roomId: String, gameId: String = null) = Secured.Authenticated {
    Action {
      implicit request =>
        val room = getRoom(roomId)
        val user = User.fromRequest(request)
        initializeGameIfNecessary(room, user.nickname)
        
        if (gameId != null && gameId != room.game.uuid) {
          // game is no longer being played
          // should not be a 200, but something else, probably a 30X (redirection )
          Ok(views.html.gameFinished(room.id, gameId, user))
        } else {
          Ok(views.html.game(room.id, room, user))
        }
    }
  }

  //
  // GET /rooms/n/games/xx-xx-x-x-x-xxx/status
  //
  def status(roomId: String, gameId: String) = Action {
    // TODO : disable browser caching on this method
    val roomOpt = rooms.get(roomId)
    if (roomOpt == None) {
      Gone("Room is closed");
    }
    val room = roomOpt.get
    val game = room.game
    if (game == null || gameId != game.uuid) {
      Gone("Game " + gameId + " is not there anymore, current one: " + game.uuid);
    }
    else {
      var state = "playing"
      if (game.isDone) {
        state = "finished"
      }
      Ok(toJson(Map(
            "game" -> toJson(
              Map(
                "duration" -> toJson(game.durationInSeconds),
                "timeLeft" -> toJson(game.secondsLeft),
                "percentageDone" -> toJson(game.percentageDone()),
                "players" -> toJson(room.players.size),
                "status" -> toJson(state)
              )
            )
      )))
    }
  }
  
  def findRoomOfPlayer(player: String): Option[Room] = {
    roomsByPlayer.get(player) match {
      case None => None
      case Some(roomId) => rooms.get(roomId)
    }
  }

  /////////// Web sockets /////////////////

  val in = Iteratee.foreach[String](messageReceived)

  def connectPlayer(player: String) = WebSocket.using[String] {
    implicit request =>
      findRoomOfPlayer(player) match {
        case None => (null, null) // TODO no connection, log
        case Some(room) => (in, room.withPlayer(player).channel)
      }
  }

  def playerDisconnected(player: String) {
    findRoomOfPlayer(player) match {
        case None => Unit //TODO log?
        case Some(room) => roomsByPlayer -= player
                           val removedPlayer: Player = room.withoutPlayer(player)
                           if(removedPlayer != null){
                             // may be disconnected but having never played so far ....
                             removedPlayer.channel.close()
                           }
                           notifySummary(room)
    }
  }
  
  def getString(jsValue: JsValue, id: String): String = {
    (jsValue \ id).as[String]
  }
  
  def parseMovement(s: String): Movement = {
    val jsonMovement = Json.parse(s) \ "movement"
    var matches = jsonMovement match {
      case JsUndefined(error) => false
      case _ => true;
    }
    if (matches) {
      try {
        new Movement(Color.withName(getString(jsonMovement, "robot")),
                   (jsonMovement \ "originRow").as[Int],
                   (jsonMovement \ "originColumn").as[Int],
                   Direction.withName(getString(jsonMovement, "direction")))
      } catch {
        case e: Exception =>
          //log exception
          println(e)
          null
      }
    } else {
      null
    }
  }

  def messageReceived(message: String) {
    val messageJson: JsValue = Json.parse(message)

    val jsonSolution = messageJson \ "solution";
    var matches:Boolean = (jsonSolution:Any) match {
      case JsUndefined(error) => false
      case _ => true;
    }
    if (matches) {
      val player: String = (jsonSolution \ "player").as[String]
      val roomOpt = findRoomOfPlayer(player)
      if (roomOpt != None) {
        val room = roomOpt.get
        val game = room.game
        val solution = (jsonSolution \ "moves").as[List[String]]
        val score = solution.length
        if (game.validate(solution.map(parseMovement))) {
          lock.acquire()
          try {
            room.withPlayer(player).scored(solution.length)
            notifySummary(room) // player=null in order to set also the local leader board
          } finally {
            lock.release()
          }
        }
      } else {
        // TODO log?
      }
    }

    val jsonLeave = messageJson \ "leave";
    matches = (jsonLeave:Any) match {
      case JsUndefined(error) => false
      case _ => true;
    }
    if (matches){
      val player: String = (jsonLeave \ "player").as[String]
      playerDisconnected(player);
    }
  }

  def notifySummary(room: Room, fromPlayer: String = null) {
    val summary: JsValue = GameSummary.fromRoom(room).toJson;
    val message: String = Json.stringify(summary)
    room.players.filter(player => player.name != fromPlayer).foreach(player => player.sendJSon(message))
  }
}
