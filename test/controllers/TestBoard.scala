package controllers

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.mvc.{Session, Cookies}

class TestBoard extends Specification {
  "redirect to current game" in running(FakeApplication()) {
    {
      // FIXME : only works because we have that user in DB ...
      // maybe not fixme, tibal is in the default user inserted into an empty DB, seems acceptable for unit tests.
      val req = RequestTestHelper.withAuthenticatedUser(FakeRequest(), "tibal")
      val result = Gaming.currentGame("default").apply(req)

      status(result) mustEqual OK
    }
  }


  "redirect to login page if not logged on" in {
    val result = Gaming.currentGame("default").apply(FakeRequest())

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
    // would need a way to inject a user in DB with name "username" ...
    withSession(req, ("username" -> userName))
  }


}