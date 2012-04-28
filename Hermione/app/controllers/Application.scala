package controllers

import play.api._
import play.api.mvc._
import util.Random
import models.{Board, DefaultBoard, Robot, Color}


object Application extends Controller {

  def index = Action {
    Ok(views.html.index("my application is ready."))
  }


  def board = Action {
    val board:Board = DefaultBoard;
    val robots:List[Robot] =   generateRobots(board)

    Ok(views.html.board(board,robots))
  }

  def generateRobots(board:Board) = {
    var robots = List[Robot]()
    for(c <- Color.values){
      val posX:Int = getRandomPosition(board.width)
      val posY:Int = getRandomPosition(board.height)
       robots ::=  new Robot(c,posX,posY)
    }
    robots
  }
  
  def getRandomPosition(maxValue:Int) = {
    Random.nextInt(maxValue)
  }


}
