package controllers

import play.api._
import play.api.mvc._
import models.DefaultBoard
import models.Robot
import models.Color


object Application extends Controller {

  def index = Action {
    Ok(views.html.index("my application is ready."))
  }


  def board = Action {

    val robots:Array[Robot] =   Array(new Robot(Color.Red, 2, 7),new Robot(Color.Blue, 5, 12),new Robot(Color.Yellow, 4, 15),new Robot(Color.Green, 12, 0))

    Ok(views.html.board(DefaultBoard,robots))
  }



}
