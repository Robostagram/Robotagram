package controllers

import scala.collection.mutable.HashMap
import play.api.mvc._
import models._
import concurrent.Lock
import play.api.libs.iteratee.Iteratee
import controllers.Authentication.Secured
import play.api.libs.json.Json.toJson
import play.api.data.Form
import play.api.libs.json.{JsUndefined, Json, JsValue}

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
  
  
  //
  // GET /rooms/n/games/current
  //
  def currentGame(roomId: String) = Secured.Authenticated {
    Action { implicit request =>
      rooms.get(roomId).map { room =>
        val user = User.fromRequest(request)
        initializeGameIfNecessary(room, user.nickname)
        Redirect(routes.Gaming.getGame(room.id, room.game.uuid))

      }.getOrElse(Results.NotFound) //no room with that id
    }
  }

  //
  // GET /rooms/n/games/xx-xx-x-x-x-xxx
  //
  def getGame(roomId: String, gameId: String = null) = Secured.Authenticated {
    Action { implicit request =>
      rooms.get(roomId).map {room =>
        val user = User.fromRequest(request)
        initializeGameIfNecessary(room, user.nickname)
        if (gameId != null && gameId != room.game.uuid) {
          // game is no longer being played
          // should not be a 200, but something else, probably a 30X (redirection )
          Ok(views.html.gameFinished(room.id, gameId, user))
        } else {
          Ok(views.html.game(room.id, room, user))
        }
      }.getOrElse(NotFound) //no room with that id
    }
  }

  //
  // GET /rooms/n/games/xx-xx-x-x-x-xxx/status
  //
  def status(roomId: String, gameId: String) = Secured.Authenticated {
    Action {
      rooms.get(roomId).map { room =>
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
      }.getOrElse(NotFound("Unknown room"))
    }
  }

  //
  // POST /rooms/:roomId/games/:gameId/solution
  //
  def submitSolution(roomId: String, gameId: String) = Secured.Authenticated {
    Action { implicit request =>
      rooms.get(roomId).map { room =>
        val user = User.fromRequest(request)
        initializeGameIfNecessary(room, user.nickname)

        if (gameId != null && gameId != room.game.uuid) {
          Gone("This game is over - score not submitted")
        } else {
          // extract posted solution from the request
          Form("solution" -> play.api.data.Forms.text).bindFromRequest.fold(
            noScore => BadRequest("No solution submitted"),
            solution => {
              findRoomOfPlayer(user.nickname).map {roomOfPlayer =>
                val game = roomOfPlayer.game
                val messageJson: JsValue = Json.parse(solution)
                val moves = (messageJson \ "moves").as[List[String]]
                val score = moves.length
                if (game.validate(moves.map(parseMovement))) {
                  lock.acquire()
                  try {
                    roomOfPlayer.withPlayer(user.nickname).scored(score)
                    notifySummary(roomOfPlayer) // player=null in order to set also the local leader board
                    Accepted("Solution accepted")
                  } finally {
                    lock.release()
                  }
                }
                else{
                  NotAcceptable("Solution not accepted")
                }
              }.getOrElse(BadRequest("unknown room for player")) // room of player does not exist
            }
          )
        }
      }.getOrElse(NotFound) //unknown roomId
    }
  }
  
  def findRoomOfPlayer(player: String): Option[Room] = {
    roomsByPlayer.get(player).map { roomIdOfPlayer =>
      rooms.get(roomIdOfPlayer)
    }.getOrElse(None)
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

    val jsonLeave = messageJson \ "leave";
    var matches = (jsonLeave:Any) match {
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
