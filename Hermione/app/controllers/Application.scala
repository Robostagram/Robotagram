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

    val robots:Array[Robot] =   Array(new Robot(Color.Red),new Robot(Color.Blue),new Robot(Color.Yellow),new Robot(Color.Green))

    Ok(views.html.board(DefaultBoard,robots))
  }



}
