import controllers.Admin
import helpers.IconColorizer
import play.api._


object Global extends GlobalSettings {

  override def onStart(app: Application) {
    IconColorizer.generate
    Admin.bootstrap
  }
}