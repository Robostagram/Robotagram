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
        User(Id(0), "hermione", "hermione"),
        User(Id(1), "kus", "hermione"),
        User(Id(2), "rom1", "hermione"),
        User(Id(3), "bzn", "hermione"),
        User(Id(4), "mithfindel", "hermione"),
        User(Id(5), "tibal", "hermione"),
        User(Id(6), "nire", "hermione")
      ).foreach(User.create)
    }
    
  }
  
}