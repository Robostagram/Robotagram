package models

import play.api.libs.json._


class GameSummary(val scores: Map[String, Int]) {
  def toJson: JsValue = {
    var scoresList:List[JsValue] = Nil
    scores.foreach(score => scoresList = JsObject(Seq("player" -> JsString(score._1), "score" -> JsNumber(score._2)))::scoresList)
    JsObject(Seq("scores" -> JsArray(scoresList.toSeq)))
  }
}

object GameSummary {
  def fromGame(game: Game):GameSummary = {
    var summary: Map[String, Int] = Map.empty;
    game.players.foreach(player => summary = summary + (player.name -> player.highScore))
    new GameSummary(summary)
  }
}
