import java.util
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
        (0, "hermione", "hermione@robotagram.com", "hermione"),
        (1, "kus", "kus@robotagram.com", "hermione"),
        (2, "rom1", "rom1@robotagram.com", "hermione"),
        (3, "bzn", "bzn@robotagram.com", "hermione"),
        (4, "mithfindel", "mithfindel@robotagram.com", "hermione"),
        (5, "tibal", "tibal@robotagram.com", "hermione"),
        (6, "nire", "nire@robotagram.com", "hermione")
      ).foreach( tup => tup match{
        case (anId, name, email, pwd) =>  User.create(Some(anId.asInstanceOf[Long]), name, email, pwd, activated_on = Some(new util.Date()))
      })
    }
    
  }
  
}