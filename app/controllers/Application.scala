package controllers

import play.api.mvc.{Action, Controller}
import play.api.Routes


object Application extends Controller {

  //
  //  GET /assets/javascripts/routes.js
  //
  // -- Javascript routing
  // generate a javascript file that declares a global variable "jsRoutes"
  // which has accessors to the application urls and provides ajax helpers
  //
  // Allows to do things like :
  //     var theUrl = jsRoutes.controllers.Gaming.getGame("myRoom", "myGame").url
  // or
  //     jsRoutes.controllers.Authentication
  //         .authenticate("http://urltoredirect.com")
  //        .ajax({
  //            data:{nickname:"tibal"},
  //            complete:function(jqXHR, textStatus){
  //                alert("Done :" + textStatus);
  //            }
  //        });
  // (the parameters to pass to the ajax() call are the same as jQuery.ajax)

  def javascriptRoutes = Action {
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        Gaming.currentGame, Gaming.getGame, Gaming.status, Gaming.connectPlayer,
        Authentication.login, Authentication.logout, Authentication.authenticate
      )
    ).as("text/javascript")
  }

}
