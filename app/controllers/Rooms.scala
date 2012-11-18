package controllers

import play.api.mvc._
import models._
import controllers.Authentication.Secured

// Room controller
object Rooms extends Controller{

  // list the last n games of a room
  def gamesByRoom(roomName:String) = Secured.AdminAuthenticated {
    Action { implicit request =>
      DbRoom.findByName(roomName).map{dbRoom =>
        Ok(views.html.rooms.gamesByRoom(dbRoom, DbGame.getLatestGamesByRoom(dbRoom.id.get, limit = 15), User.fromRequest))
      }
        .getOrElse(NotFound)
    }
  }

  def previewGame(roomName: String, gameId : String) = Secured.AdminAuthenticated {
    Action {implicit request =>
      DbRoom.findByName(roomName).map {dbRoom =>
        Game.load(dbRoom.name, gameId).map{g =>
          Ok(views.html.rooms.previewGame(dbRoom, g, User.fromRequest, DbScore.findByGame(gameId)))
        }
          .getOrElse(NotFound)
      }
        .getOrElse(NotFound)

    }
  }

}
