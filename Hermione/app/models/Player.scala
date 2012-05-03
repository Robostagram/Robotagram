package models

import play.api.libs.iteratee.{Enumerator, PushEnumerator}

class Player(val name: String) {
  var highScore: Int = -1;
  var channel: PushEnumerator[String] = Enumerator.imperative[String]();

  def scored(newScore: Int) {
    if (highScore == -1 || highScore > newScore)  {
      highScore = newScore
    }
  }

  def sendJSon(message: String) {
    channel.push(message);
  }
}
