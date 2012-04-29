package models

import play.api.libs.json.JsValue
import play.api.libs.iteratee.PushEnumerator

class Player(val name: String) {
  var highScore: Int = 0;
  var channel: PushEnumerator[JsValue] = null;

  def scored(newScore: Int) {
    highScore = if (highScore < newScore) newScore else highScore
  };

  def sendJSon(message: JsValue) {
    channel.push(message);
  }
}
