package tests

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.mvc.{Session, Cookies}

class TestBoard extends Specification {
  "respond to the board Action if logged in" in running(FakeApplication()) {
    {

      var req = RequestTestHelper.withAuthenticatedUser(FakeRequest(), "john")
      val result = controllers.Application.newGame.apply(req)

      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      charset(result) must beSome("utf-8")
      contentAsString(result) must contain("<table id=\"boardGame\">")
    }
  }


  "redirect to login page if not logged on" in {
    val result = controllers.Application.newGame.apply(FakeRequest())

    status(result) mustNotEqual OK
    status(result) mustEqual SEE_OTHER
    header("Location", result).get must startWith("/login?redirectTo") // redirect to index

  }
}


// inspired from: https://github.com/athieriot/Play20/blob/aeeb24d10c2ef6e1bd9e853640fb490b1fe77662/framework/src/play-test/src/main/scala/play/api/test/Fakes.scala

object RequestTestHelper {

  def withSession(req: FakeRequest[play.api.mvc.AnyContent], newSessions: (String, String)*): FakeRequest[play.api.mvc.AnyContent] = {
    req.withHeaders(play.api.http.HeaderNames.COOKIE ->
      Cookies.merge(req.headers.get(play.api.http.HeaderNames.COOKIE).getOrElse(""),
        Seq(Session.encodeAsCookie(new Session(req.session.data ++ newSessions)))
      )
    )
  }

  def withAuthenticatedUser(req: FakeRequest[play.api.mvc.AnyContent], userName: String): FakeRequest[play.api.mvc.AnyContent] = {
    withSession(req, ("username" -> userName))
  }


}
