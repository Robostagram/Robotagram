package models

import play.api.libs.json._


class GameSummary(val scores: Map[String, Int], val endTime:Long, val totalDuration:Long) {
  def toJson: JsValue = {
    var scoresList:List[JsValue] = Nil
    scores.foreach(score => scoresList = JsObject(Seq("player" -> JsString(score._1), "score" -> JsNumber(score._2)))::scoresList)
    JsObject(Seq("scores" -> JsArray(scoresList.toSeq), "end" -> JsNumber(endTime), "duration" -> JsNumber(totalDuration)))
  }
}

object GameSummary {
  def fromRoom(room: Room):GameSummary = {
    var summary: Map[String, Int] = Map.empty;
    val game = room.game
    room.players.foreach(player => summary = summary + (player.name -> player.highScore))
    new GameSummary(summary, game.endTime, game.durationInSeconds*1000)
  }
}
