package controllers

import play.api.mvc._
import models._

// Room controller
object Rooms extends CookieLang{

  // list the last n games of a room
  def gamesByRoom(roomName:String) = UserAwareAction { implicit request =>
      DbRoom.findByName(roomName).map{dbRoom =>
        Ok(views.html.rooms.gamesByRoom(dbRoom, DbGame.getLatestGamesByRoom(dbRoom.id.get, limit = 15), DbUser.fromRequest()))
      }
        .getOrElse(NotFound)
    }

  def previewGame(roomName: String, gameId : String) =UserAwareAction {implicit request =>
      DbRoom.findByName(roomName).map {dbRoom =>
        Game.load(dbRoom.name, gameId).map{g =>
          Ok(views.html.rooms.previewGame(dbRoom, g, DbUser.fromRequest(), DbScore.findByGame(gameId)))
        }
          .getOrElse(NotFound)
      }
        .getOrElse(NotFound)

    }

}
