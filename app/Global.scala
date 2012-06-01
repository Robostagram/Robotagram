import play.api._

import models._
import anorm._

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
    if(User.findAll.isEmpty) {
      
      Seq(
        User(Id(0), "hermione", "hermione@robotagram.com", "hermione"),
        User(Id(1), "kus", "kus@robotagram.com", "hermione"),
        User(Id(2), "rom1", "rom1@robotagram.com", "hermione"),
        User(Id(3), "bzn", "bzn@robotagram.com", "hermione"),
        User(Id(4), "mithfindel", "mithfindel@robotagram.com", "hermione"),
        User(Id(5), "tibal", "tibal@robotagram.com", "hermione"),
        User(Id(6), "nire", "nire@robotagram.com", "hermione")
      ).foreach(User.create)
    }
    
  }
  
}