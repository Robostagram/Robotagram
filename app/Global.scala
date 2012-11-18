import java.util
import play.api._

import controllers.Admin
import models._
import anorm._
import util.Random

object Global extends GlobalSettings {
  
  override def onStart(app: Application) { Admin.insert }
  
}