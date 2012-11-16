import java.util
import play.api._

import models._
import anorm._
import util.Random

object Global extends GlobalSettings {
  
  override def onStart(app: Application) {
    InitialData.insert()
  }
  
}

/**
 * Initial set of data to be imported 
 * in the sample application.
 */
object InitialData {
  
  def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(str)
  
  def insert() = {
    // populate users with some known names ;)
    if(DbUser.findAll.isEmpty) {
      
      Seq(
        (0, "hermione", "hermione@robotagram.com", "hermione"),
        (1, "kus", "kus@robotagram.com", "hermione"),
        (2, "rom1", "rom1@robotagram.com", "hermione"),
        (3, "bzn", "bzn@robotagram.com", "hermione"),
        (4, "mithfindel", "mithfindel@robotagram.com", "hermione"),
        (5, "tibal", "tibal@robotagram.com", "hermione"),
        (6, "nire", "nire@robotagram.com", "hermione")
      ).foreach( tup => tup match{
        case (anId, name, email, pwd) =>  DbUser.create(Some(anId.asInstanceOf[Long]), name, email, pwd, activated_on = Some(new util.Date()))
      })
    }

    // populate the default boards (take the standard board and then make variations ...
    if(DbBoard.findAll.isEmpty){
      val standardBoard = Board.boardFromFile("app/resources/Standard.board", 0, "no name");
      val QUARTER_COMBOS = Array((1, 2, 3), (1, 3, 2), (2, 1, 3), (2, 3, 1), (3, 1, 2), (3, 2, 1))
      QUARTER_COMBOS.zipWithIndex.foreach(tup =>  tup match {
        case (combination, index) =>
          val boardId = (index + 1).asInstanceOf[Long] //start at one
          val boardName = "Standard.board (" + combination._1 + ", " + combination._2 + ", " + combination._3 + ")"
          val boardData = Board.boardToString(standardBoard.transformQuarters(combination))
          DbBoard.create(Some(boardId), boardName, boardData)
      })
    }

    // create the 2 default rooms ...
    if(DbRoom.findAll.isEmpty){
      DbRoom.create(Some(1), "default")
      DbRoom.create(Some(2), "default2")
    }

    /*DbGame.getActiveInRoomOrCreate("default", () => {
      var idOfBoardToLoad = new Random().nextInt(6) + 1
      var b = Board.loadById(idOfBoardToLoad).get
      var g = Goal.randomGoal()
      var robots = Board.randomRobots(b)
      DbGame.prepareGameToStore(1, Game.DEFAULT_GAME_DURATION,b, g, robots)
    }) */
    
  }
  
}