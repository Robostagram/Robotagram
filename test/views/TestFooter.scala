package views

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.mvc.Flash
import play.api.i18n.Lang
import models._
import play.api.test.FakeApplication

class TestFooter extends Specification {
  "render main template" in running(FakeApplication()){
    val html = views.html._layout("title",None)(null)(null)(new Flash(),null,new Lang("fr"))
    contentType(html) must equalTo("text/html")
    //contentAsString(html) must contain("app.footer")
    contentAsString(html) must contain("Apache License v2.0") // check the actual translated text


  }
}
