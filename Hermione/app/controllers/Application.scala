package controllers

import play.api.mvc._
import models._
import concurrent.Lock


object Application extends Controller {
  var game: Game = null;
  val lock: Lock = new Lock();

  def index = Action {
    lock.acquire();

    Ok(views.html.index("my application is ready."))
  }

  def newGame = Action {
    lock.acquire();
    try {
      if (game == null || game.isDone) {
        game = Game.randomGame()
      }
    } catch {
      case e => InternalServerError("WTF ?");
    } finally {
      lock.release();
    };
    Ok(views.html.board(game))
  }

  def reloadBoard = Action {
    lock.acquire();
    try {
      if (game == null) {
        game = Game.randomGame()
      } else if (game.isDone) {
        Ok("Game is done, please reload to start a new one")
      }
    } catch {
      case e => InternalServerError("WTF ?");
    } finally {
      lock.release();
    };
    Ok(views.html.renderBoard(game))
  }
}
