package controllers

import play.api.mvc._
import models._
import concurrent.Lock
import play.api.libs.iteratee.{Iteratee, Enumerator}
import play.api.libs.json.{Json, JsValue}
import controllers.Authentication.Secured

object Application extends Controller {
  var game: Game = null;
  val lock: Lock = new Lock();

  def newGame(score: Int = 0) = Secured.Authenticated {
    Action {
      implicit request =>
        val user = User.fromRequest(request)
        lock.acquire();
        try {
          if (game == null || game.isDone) {
            game = Game.randomGame()
          }
        } catch {
          case e => InternalServerError("WTF ? " + e);
        } finally {
          lock.release();
        };
        val player: Player = game.withPlayer(user.nickname)
        player.scored(score);
        Ok(views.html.game(game, player, user))
    }
  }

  def reloadBoard = Action {
    lock.acquire();
    try {
      if (game == null) {
        game = Game.randomGame()
      } else if (game.isDone) {
        Ok("Game is done, please <a href=\"/\">start a new one</a>.")
      }
    } catch {
      case e => InternalServerError("WTF ? " + e);
    } finally {
      lock.release();
    };
    Ok(views.html.renderBoard(game))
  }

  def scores = Action {
    Ok(views.html.scores(game));
  }

  def progress = Action {
    Ok("" + game.percentageDone());
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
        val summary:JsValue = GameSummary.fromGame(game).toJson;
        game.players.foreach(player => player.sendJSon(summary))
      }
    } finally {
      lock.release();
    };
  }
}
