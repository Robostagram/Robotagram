package controllers

import play.api.mvc._
import models.{DbRoom, DbGame, User, DbBoard}

// Room controller
object Rooms extends Controller{

  // list the last n games of a room
  def gamesByRoom(roomName:String) = Action { implicit request =>
    DbRoom.findByName(roomName).map{dbRoom =>
      Ok(views.html.rooms.gamesByRoom(dbRoom, DbGame.getLatestGamesByRoom(dbRoom.id.get, limit = 15), User.fromRequest))
    }
    .getOrElse(NotFound)

  }

}
