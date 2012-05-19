package tests

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.mvc.Flash
import play.api.i18n.Lang
import models._

class TestFooter extends Specification {
  "render main template" in {
    val html = views.html.main("title",new User("nick"))(null)(null)(new Flash(),null,new Lang("fr"))
    contentType(html) must equalTo("text/html")
    contentAsString(html) must contain("app.footer")
  }
}
