package controllers

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

object Application extends Controller {
  // sooner or later, handle games in several rooms ... so far, just use the default room
  var game: Game = null;
  val lock: Lock = new Lock();


  private def initializeGameIfNecessary(playerName:String) {
    lock.acquire();
    try {
      var createNewGame: Boolean = game == null || game.isDone;
      if (createNewGame) {
        if(game != null) game.players.foreach(player => player.channel.close())
        game = Game.randomGame()
      }
      if (!createNewGame) {  // if game already existed, notify other users of new user
        notifySummary(playerName)
      }
    } catch {
      case e => InternalServerError("WTF? " + e);
    } finally {
      lock.release();
    };
  }

  //
  // GET /rooms/n/games/current
  //
  def currentGame(roomId: String) = Secured.Authenticated {
    Action {
      implicit request =>
        val user = User.fromRequest(request)
        if (roomId != "default") {
          NotFound("404 de la mort : room '" + roomId + "' does not exist. Only the room 'default' exists so far")
        }
        else {
          // find latest game for room with that Id
          initializeGameIfNecessary(user.nickname)
          Redirect(routes.Application.getGame(roomId, game.uuid))
        }
    }
  }


  //
  // GET /rooms/n/games/xx-xx-x-x-x-xxx
  //
  def getGame(roomId: String, gameId: String) = Secured.Authenticated {
    Action {
      implicit request =>
        if (roomId != "default") {
          NotFound("404 de la mort : room '" + roomId + "' does not exist. Only the room 'default' exists so far")
        }
        else {
          val user = User.fromRequest(request)
          initializeGameIfNecessary(user.nickname)
          if (gameId != game.uuid) {
            // game is no longer being played
            // should not be a 200, but something else, probably a 30X (redirection )
            Ok(views.html.gameFinished(roomId, gameId, user))
          } else {

            val player: Player = game.withPlayer(user.nickname)
            Ok(views.html.game(roomId, game, player, user))
          }
        }
    }
  }

  //
  // GET /rooms/n/games/xx-xx-x-x-x-xxx/status
  //
  def status(roomId: String, gameId: String) = Action {
    // TODO : disable browser caching on this method
    if (game == null || gameId != game.uuid) {
      Gone("Game is not there anymore");
    }
    else {
      var state = "playing"
      if (game.isDone) {
        state = "finished"
      };
      Ok(toJson(Map(
            "game" -> toJson(
              Map(
                "duration" -> toJson(game.durationInSeconds),
                "timeLeft" -> toJson(game.secondsLeft),
                "percentageDone" -> toJson(game.percentageDone()),
                "players" -> toJson(game.players.size),
                "status" -> toJson(state)
              )
            )
      )))
    }
  }

  /////////// Web sockets /////////////////

  val in = Iteratee.foreach[String](messageReceived)

  def connectPlayer(player: String) = WebSocket.using[String] {
    implicit request =>
      (in, game.withPlayer(player).channel)
  }

  def playerDisconnected(player: String) {
    if (game != null){
      val removedPlayer: Player = game.withoutPlayer(player)
      if(removedPlayer != null){
        // may be disconnected but having never played so far ....
        removedPlayer.channel.close()
      }
    }
    notifySummary()
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
      val solution = (jsonSolution \ "moves").as[List[String]]
      val score = solution.length
	  if(game.validate(solution.map(parseMovement))) {
        lock.acquire()
        try {
          game.withPlayer(player).scored(solution.length)
          notifySummary() // null in order to set also the local leader board
        } finally {
          lock.release()
        }
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

  def notifySummary(fromPlayer: String = null) {
    if (game != null) {
      val summary: JsValue = GameSummary.fromGame(game).toJson;
      val message: String = Json.stringify(summary)
      game.players.filter(player => player.name != fromPlayer).foreach(player => player.sendJSon(message))
    }
  }
}
