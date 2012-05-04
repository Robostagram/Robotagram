package controllers

import play.api.mvc._
import models._
import concurrent.Lock
import play.api.libs.iteratee.{Iteratee, Enumerator}
import controllers.Authentication.Secured
import play.api.data.Form
import play.api.libs.json.Json.toJson
import play.api.libs.json.JsValue

object Application extends Controller {
  // sooner or later, handle games in several rooms ... so far, just use the default room
  var game: Game = null;
  val lock: Lock = new Lock();


  private def initializeGameIfNecessary() {
    lock.acquire();
    try {
      // get game from Id or load random new one ?
      if (game == null || game.isDone) {
        game = Game.randomGame()
      }
    } catch {
      case e => InternalServerError("WTF ? " + e);
    } finally {
      lock.release();
    };
  }

  //
  // GET /rooms/n/games/current
  //
  def currentGame(roomId: Int) = Secured.Authenticated {
    Action {
      implicit request =>
        val user = User.fromRequest(request)
        if (roomId != 0) {
          NotFound("404 de la mort : Only the room 0 exists so far")
        }
        else {
          // find latest game for room with that Id
          initializeGameIfNecessary()
          Redirect(routes.Application.getGame(roomId, game.uuid))
        }
    }
  }


  //
  // GET /rooms/n/games/xx-xx-x-x-x-xxx
  //
  def getGame(roomId: Int, gameId: String) = Secured.Authenticated {
    Action {
      implicit request =>

        if (roomId != 0) {
          NotFound("404 de la mort : Only the room 0 exists so far")
        }
        else {

          val user = User.fromRequest(request)
          initializeGameIfNecessary()
          if (gameId != game.uuid) {
            // game is no longer being played
            // should not be a 200, but something else, probably a 30X (redirection )
            Ok(views.html.gameFinished(roomId, gameId, user))
          } else {

            val player: Player = game.withPlayer(user.nickname)
            player.scored(0);
            Ok(views.html.game(roomId, game, player, user))
          }
        }
    }
  }


  // post my own score
  // POST /rooms/n/games/xx-xxx-xx/score
  //
  // the score is posted - need to process the request posted info
  def submitScore(roomId: Int, gameId: String) = Secured.Authenticated {
    Action {
      implicit request =>
        val user = User.fromRequest(request)
        Form("score" -> play.api.data.Forms.number(min = 0)).bindFromRequest.fold(
          noScore => BadRequest("Missing a positive  score ...."),
          submittedScore => {
            if (game != null && !game.isDone && game.uuid == gameId) {
              val player: Player = game.withPlayer(user.nickname)
              player.scored(submittedScore);
              Accepted("Score updated : " + submittedScore);
            }
            else {
              Gone("Game is done or unknown. Score not submitted");
            }
          }
        )
    }
  }


  //
  // GET /rooms/n/games/xx-xx-x-x-x-xxx/scores
  //
  def scores(roomId: Int, gameId: String) = Action {
    // TODO : disable browser caching on this method
    // TODO: handle room and game Id
    Ok(views.html.scores(game));
  }


  def status(roomId: Int, gameId: String) = Action {
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
                "players" -> toJson(game.players.count(p => true)),
                "status" -> toJson(state)
              )
            )
      )))
    }
  }

  /////////// Web sockets /////////////////

  val in = Iteratee.foreach[JsValue](summaryReceived)

  def connectPlayer(player: Player) = WebSocket.using[JsValue] {
    implicit request =>
    // Send a single 'Hello!' message
      val out = Enumerator.imperative[JsValue]()
      game.withPlayer(player.name).channel = out
      (in, out)
  }

  def summaryReceived(jsonScore: JsValue) {
    lock.acquire();
    try {
      if (game != null) {
        game.withPlayer((jsonScore \ "player").as[String]).scored((jsonScore \ "score").as[Int]);
      }
    } finally {
      lock.release();
    };
    notifySummary()
  }

  def notifySummary() {
    lock.acquire();
    try {
      if (game != null) {
        val summary: JsValue = GameSummary.fromGame(game).toJson;
        game.players.foreach(player => player.sendJSon(summary))
      }
    } finally {
      lock.release();
    };
  }
}
