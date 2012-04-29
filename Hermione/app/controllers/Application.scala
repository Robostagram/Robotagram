package controllers

import play.api.mvc._
import models._
import concurrent.Lock


object Application extends Controller {
  var game: Game = null;
  val lock: Lock = new Lock();

  def index = Action {
    Ok(views.html.index())
  }

  def newGame(user:String, score:Int = 0) = Action {
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
    val player: Player = game.withPlayer(user)
    player.scored(score);
    Ok(views.html.board(game, player))
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
    Ok(game.percentageDone().toString);
  }
}
