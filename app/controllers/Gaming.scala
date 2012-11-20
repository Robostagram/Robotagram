package controllers

import play.api.mvc._
import models._
import concurrent.Lock
import play.api.libs.iteratee.Iteratee
import controllers.Authentication.Secured
import play.api.libs.json.Json.toJson
import play.api.data.Form
import play.api.libs.json._
import play.api.libs.json.JsUndefined
import play.api.libs.json.JsString
import play.api.libs.json.JsObject
import play.api.Logger
import collection.mutable

object Gaming extends CookieLang {

  val lock: Lock = new Lock()
  
  //
  // GET /rooms/n/games/current
  // access the current active game of the room ... or initialize a new one
  def currentGame(roomName: String) = Secured.Authenticated {
    Action { implicit request =>
      val user = User.fromRequest(request).get
      DbRoom.findByName(roomName).map { room =>
        val wsRoom = WsManager.room(roomName).get
        // user is already in a game in this room? close his previous WS channel
        wsRoom.players.get(user.name).map{ wsPlayer =>
          wsPlayer.sayGoodBye()
        }.getOrElse()
        val g = Game.getActiveInRoomOrCreateRandom(room.id.get)
        val scores = playersAndScores(roomName, g.id)
        Ok(views.html.gaming.game(room, g, scores, user))
      }.getOrElse(Results.NotFound) //no room with that id
    }
  }

  // kick a player from all rooms he's connected to
  private def kick(username: String): Option[Unit] = {
    WsManager.roomForPlayer(username).map{r=>
      r.player(username).map(p=> p.send(makeJsonMessage("player.kickout", Seq[String]("A connexion to the game was open from somewhere else and request this one to be closed."))))
      r.kickPlayer(username)
    }
  }

  //
  // POST /rooms/n/eject
  // force closing the existing connections to this room for the current player
  def eject(roomName: String, redirectTo: Option[String] = None) = Secured.Authenticated {
    Action { implicit request =>
      kick(User.fromRequest(request).get.name)
      // redirect to targetUrl ... or to current game in the room
      redirectTo.map(url =>
        Redirect(url)
      )
      .getOrElse(
        // no redirection url : redirect home
        Redirect(routes.Home.index())
      )
    }
  }


  // history of a board ... get access to previously played game
  // if game is still active, redirect to the game active board ("current")
  // GET /rooms/n/games/xx-xx-x-x-x-xxx
  //
  def getGame(roomName: String, gameId: String = null) = Secured.Authenticated {
    Action { implicit request =>
      val user = User.fromRequest(request)
      DbRoom.findByName(roomName).map{ dbRoom =>
        DbGame.findByRoomAndId(roomName, gameId).map { game =>
          if(game.valid_until.getTime > System.currentTimeMillis()){
            // game is active
            Redirect(routes.Gaming.currentGame(dbRoom.name))
          }else{
            Ok(views.html.gaming.gameFinished(dbRoom.name, gameId, user))
          }
        }.getOrElse(NotFound)
      }.getOrElse(NotFound)
    }
  }

  //
  // GET /rooms/n/games/xx-xx-x-x-x-xxx/status
  //
  def gameStatus(roomName: String, gameId: String) = Secured.Authenticated {
    Action {
      DbRoom.findByName(roomName).map{ dbRoom =>
        Game.load(roomName, gameId).map { game =>
          var state = "playing"
          if (game.isDone) {
            state = "finished"
          }
          Ok(toJson(Map(
            "game" -> toJson(
              Map(
                "duration" -> toJson(game.durationInSeconds),
                "timeLeft" -> toJson(game.secondsLeft()),
                "status" -> toJson(state)
              )
            ))))

        }.getOrElse(Gone("Game " + gameId + " does not exist for room " + roomName))
      }
      .getOrElse(NotFound("room " + roomName + " does not exist"))
    }
  }

  // list of players and scores (persisted + players in the room)
  private def playersAndScores(roomName:String, gameId:String) : Seq[(String, Option[Int])] = {
    val wsRoom = WsManager.room(roomName).get
    var scores: mutable.HashMap[String, Option[Int]] = new mutable.HashMap[String, Option[Int]]()

    DbScore.findByGame(gameId).foreach{s=>
      scores += ((s.playerName, Some(s.score)))
    }
    wsRoom.players.keys.foreach(p =>
      if (scores.get(p).isEmpty){
        scores += ((p, None))
      }
    )
    scores.toList
  }

  //
  // GET /rooms/n/games/xx-xx-x-x-x-xxx/scores
  // scores and participants in a game ...
  //
  def gameScores(roomName: String, gameId: String) = Secured.Authenticated {
    Action { implicit request =>
      DbRoom.findByName(roomName).map{ dbRoom =>
        DbGame.findByRoomAndId(roomName, gameId).map { game =>
          var scoresList:List[JsValue] = Nil
          playersAndScores(roomName, gameId).foreach(score => scoresList = JsObject(Seq(
            "player" -> JsString(score._1),
            "score" -> JsNumber(score._2.getOrElse[Int](0))))::scoresList)
          Ok(toJson(JsObject(Seq("scores" -> JsArray(scoresList.toSeq)))))
        }.getOrElse(Gone("Game " + gameId + " does not exist for room " + roomName))
      }.getOrElse(NotFound("room " + roomName + " does not exist"))
    }
  }

  //
  // POST /rooms/:roomId/games/:gameId/solution
  //
  def submitSolution(roomName: String, gameId: String) = Secured.Authenticated {
    Action { implicit request =>
      val user = User.fromRequest.get
      DbRoom.findByName(roomName).map{ dbRoom =>
        Game.load(roomName, gameId).map { game =>
          if(game.isDone){
            Gone("This game is over - score not submitted")
          }
          else{
            // extract posted solution from the request
            Form("solution" -> play.api.data.Forms.text).bindFromRequest.fold(
              noScore => BadRequest("No solution submitted"),
              solution => {
                logMessage(roomName, user.name, "<SOLUTION>" + solution)
                val messageJson: JsValue = Json.parse(solution)
                val moves = (messageJson \ "moves").as[List[String]]
                val score = moves.length
                if (game.validate(moves.map(parseMovement))) {
                  lock.acquire()
                  try {
                    //TODO: persist score !
                    DbScore.insert(game.id, user.id, score, solution)
                    notifyRoom(roomName, "room.player.submittedSolution", Seq[String](user.name, score.toString))
                    Accepted("Solution accepted")
                  } finally {
                    lock.release()
                  }
                }
                else{
                  NotAcceptable("Solution not accepted")
                }
              }
            )
          }
        }
        .getOrElse(NotFound) // unknown game in room
      }.getOrElse(NotFound) //unknown roomId
    }
  }

  /////////// Web sockets /////////////////


  def connectPlayer(roomName : String,player: String) = WebSocket.using[String] { implicit request =>
      logMessage(roomName, player, "<CONNECTING>")
      val wsPlayer = WsManager.room(roomName).get.join(player)
      // make an incoming channel to receive messages from that user
      val incomingPlayerChannel = Iteratee
        .foreach[String]( s=> messageReceivedFromPlayer(roomName, player, s)) //bind on incoming messages
        .mapDone{ _ => // handle client closing connection
          //forget about that user in that room
          WsManager.room(roomName).map{r=>
            r.forgetPlayer(player)
          }
          notifyRoom(roomName, "room.player.disconnected", Seq[String](player))
          logMessage(roomName, player, "<DISCONNECTED>")
        }
      notifyRoom(roomName, "room.player.connected", Seq[String](player))
      // plug in the input and output ...
      (incomingPlayerChannel, wsPlayer.channel) //TODO: add user to the room and notify everybody in the room (new player)
  }

  def logMessage(roomName:String, playerName:String, message:String){
    Logger.debug("[" + roomName + "]" + playerName + ">" + message)
  }
  
  def getString(jsValue: JsValue, id: String): String = {
    (jsValue \ id).as[String]
  }
  
  def parseMovement(s: String): Movement = {
    val jsonMovement = Json.parse(s) \ "movement"
    val matches = jsonMovement match {
      case JsUndefined(error) => false
      case _ => true
    }
    if (matches) {
      try {
        new Movement(Color.withName(getString(jsonMovement, "robot")),
                   (jsonMovement \ "originRow").as[Int],
                   (jsonMovement \ "originColumn").as[Int],
                   Direction.withName(getString(jsonMovement, "direction")))
      } catch {
        case e: Exception =>
          //log exception
          Logger.error("Error while parsing the solution", e)
          null
      }
    } else {
      null
    }
  }

  def messageReceivedFromPlayer( roomName:String, playerName:String, message: String) {
    logMessage(roomName, playerName, "IN-MSG:" + message)
    // we don't specially care about the messages actually ...
    // we get the disconnection of the user from the Iteratee.mapDone
  }


  def makeJsonMessage(messageType:String, messageArgs:Seq[String] = null) : String = {
    var jsonArgs:JsArray = new JsArray()

    if (messageArgs != null){
      jsonArgs = JsArray( messageArgs.map(s => JsString(s)) )
    }

    Json.stringify( JsObject(Seq("args" -> jsonArgs, "type" -> JsString(messageType))))
  }

  // send something to all players connected to a room
  def notifyRoom(roomName:String, messageType:String, messageArgs:Seq[String] = null){
    val msg = makeJsonMessage(messageType, messageArgs)
    logMessage(roomName, "ALL", "OUT-MSG:" + msg)
    WsManager.room(roomName).map{r=>
      r.sendAll(msg)
    }
  }
}
