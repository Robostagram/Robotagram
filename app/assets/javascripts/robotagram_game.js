//=======================
// module robotagram.game
// ======================
//
// contains the game logic on client side ...

// creating root name space robotagram if not done already ...
window["robotagram"] = window["robotagram"] || {} ; //initialize robotagram root name space if not done

// creating name space robotagram.game
// (using the patterns described in : http://www.codethinked.com/preparing-yourself-for-modern-javascript-development )
window["robotagram"]["game"] = (function($, undefined){

// global window object
var $window = $(window); // we are going to use it quite a lot anyway

// ------
// EVENTS
// ------
var REQUEST_ROBOT_MOVE = "requestMove.robot.robotagram";
function requestRobotMove(direction, color){$window.trigger(REQUEST_ROBOT_MOVE, [direction, color]);}

var EVENT_ROBOT_MOVING = "moving.robot.robotagram";
function notifyRobotMoving(direction, color){$window.trigger(EVENT_ROBOT_MOVING, [direction, color]);}
var EVENT_ROBOT_MOVED = "moved.robot.robotagram";
function notifyRobotMoved(direction, color){$window.trigger(EVENT_ROBOT_MOVED, [direction, color]);}

var EVENT_GAME_TIMEUP = "timeUp.game.robotagram";
function notifyGameTimeUp(){$window.trigger(EVENT_GAME_TIMEUP);}
var EVENT_GAME_SOLVED = "solved.game.robotagram";
function notifyGameSolved(numberOfMoves){$window.trigger(EVENT_GAME_SOLVED, [numberOfMoves]);}

// ---------
// CONSTANTS
// ---------
var ROBOT_COLORS = ['Blue', 'Red', 'Yellow', 'Green'];
var DIRECTION_UP = "Up",
    DIRECTION_LEFT = "Left",
    DIRECTION_DOWN = "Down",
    DIRECTION_RIGHT = "Right";
var DIRECTIONS = [DIRECTION_UP, DIRECTION_LEFT, DIRECTION_DOWN, DIRECTION_RIGHT];

var REFRESH_LOOP_REPEAT_TIME = 300; /* refresh time on client side . */
// for polling, we refresh not too often when there is lots of time left ...
// we poll a bit more often as we get closer to the deadline
var SERVER_POLLING_REPEAT_TIME_MIN = 2000; // never refresh more often than that.

// Keyboard
// --------
// movements
var KEY_LETTER_I = 73; // I : up
var KEY_LETTER_J = 74; // J : left
var KEY_LETTER_K = 75; // K : down
var KEY_LETTER_L = 76; // L : right
// arrows
var KEY_ARROW_UP    = 38;
var KEY_ARROW_LEFT  = 37;
var KEY_ARROW_DOWN  = 40;
var KEY_ARROW_RIGHT = 39;
// undo/redo
var KEY_LETTER_X = 88; // X : undo
var KEY_LETTER_C = 67; // C : redo
// select another robot
var KEY_LETTER_S = 83; // S : previous
var KEY_LETTER_D = 68; // D : next




// ---------------
// PRIVATE METHODS
// ---------------

// Game dynamics
// -------------

var currentGame = {
    gameId: undefined,
    roomId: undefined,
    playerName: undefined,
    duration: undefined,
    secondsLeft: undefined,
    gamePhase: undefined,
    moves : new Array(),
    undoIndex : 0,
    gameSocket: null,
    gameIsOn: false   // is there a game currently being played ?
};

// the entry point for the game loop
function init(gameParameters){
    // store the game information
    currentGame.roomId = gameParameters.roomId;
    currentGame.gameId = gameParameters.gameId;
    currentGame.playerName = gameParameters.playerName;
    currentGame.duration = gameParameters.duration;
    currentGame.secondsLeft = gameParameters.secondsLeft;
    currentGame.gamePhase = gameParameters.gamePhase;
    
    // prepare the board
    //select the robot corresponding to the objective on page load
    selectRobotOfObjective();

    // init the websocket stuff ...
    connectPlayer();
    currentGame.gameIsOn = currentGame.gamePhase != PHASEID_SHOW_SOLUTION;

    // when leaving the window, disconnect the user
    window.onbeforeunload = function() {
        // the game is finished ... just disconnect
        if(!currentGame.gameIsOn){ // no game is playing ... no need to ask confirmation before leaving
            return
        }
        // should probably ask confirmation to user ??
        return $_("game.leave.gameIsActive.confirm");
    };
    
    if(!currentGame.gameIsOn) {
      notifyGameTimeUp();
    } else {
      // launch the client-site countdown (frequent updates)
      doClientRefreshLoop();
      // launch the server polling to resync timer and game status (less frequent)
      doServerRefreshLoop();

      // set up actions on game events (time up, solution found, move robot etc)
      bindGameEventHandlers();

      // make keyboard and mouse trigger the events ...
      setUpGameControlHandlers();
    }
}


function bindGameEventHandlers(){
    // INPUT from the user
    $window.on(REQUEST_ROBOT_MOVE, function(e, direction, color){
        //console.debug(e.type, e.namespace, direction, color);
        moveRobot(color, direction);//keepHistory
    });

    // GAME events
    // do it only once
    $window.one(EVENT_GAME_TIMEUP, function(e){
        //console.debug(e.type, e.namespace);
        currentGame.gameIsOn = false; // it's ok to leave the page, now
        $("#endOfGameModal").modal('show');
        $("#winModal").modal('hide');
    });

    $window.on(EVENT_GAME_SOLVED, function(e, numberOfMoves){
        //console.debug(e.type, e.namespace, numberOfMoves);
        $("#winModal").modal('show');
    });
}

function refreshMovementCounter(){
    $("#currentScore").text(currentGame.undoIndex + "");
}

function currentState(){
    return currentGame;
}


// Robot selection
// ---------------

// get the index of the selected robot in the ROBOT_COLORS array
// returns 0 if no robot is selected
function getIndexOfCurrentlySelectedRobot() {
    var $currentSelected = $(".robot.selected");
    var curColorIndex = -1; //blue by default
    if ($currentSelected.length > 0) {
        $.each(ROBOT_COLORS, function(index, colorName) {
            if ($currentSelected.hasClass(colorName)) {
                curColorIndex = index;
                return false; //stop the loop here !
            }
            return true; //continue the loop
        });
    }
    return curColorIndex;
}

// select the next robot (order defined by ROBOT_COLORS)
function selectNextRobot() {
    var curColorIndex = getIndexOfCurrentlySelectedRobot();
    curColorIndex = (curColorIndex + 1) % ROBOT_COLORS.length;
    selectByColor(ROBOT_COLORS[curColorIndex]);
}

// select the previous robot (order defined by ROBOT_COLORS)
function selectPreviousRobot() {
    var curColorIndex = getIndexOfCurrentlySelectedRobot();
    curColorIndex = (curColorIndex + ROBOT_COLORS.length - 1) % ROBOT_COLORS.length;
    selectByColor(ROBOT_COLORS[curColorIndex]);
}

// mark the robot with given color as selected
function selectByColor(colorName) {
    $(".selected").toggleClass("selected"); //remove selected on currently selected stuff
    $("td .robot." + colorName).toggleClass("selected");
}

function selectRobotOfObjective(){
    $(".selected").removeClass("selected");
    $("#robotForObjective").addClass("selected");
}



// Robot moves
// -----------

// trigger the event that says "move the robot to ..."
function requestSelectedRobotMovement(direction){
    var selected = getIndexOfCurrentlySelectedRobot()
    if (selected >= 0 && selected < ROBOT_COLORS.length) {
        var color = ROBOT_COLORS[selected];
        requestRobotMove(direction, color);
    } else {
        alert($_("game.noselection"))
    }
}

var moving = false;

// move the robot of a given color in the requested direction
// the direction is one of the strings from DIRECTIONS
function moveRobot(color, direction, keepHistory) {
    if (moving) {
        return;
    }
    var $robot = $("td .robot." + color), originCell, destinationCell = null, previousDestination, nextDestination;
    if ($robot.length == 0) {
        // pas de robot
        console.log("a robot could not be found with color " + color);
        return;
    }

    originCell = $robot.closest("td.cell");
    // seulement si le robot est bien dans une cellule, pas en déplacement
    if (originCell.length > 0) {
        originCell = originCell.first();

        previousDestination = originCell;
        nextDestination = nextCell(previousDestination, direction);
        while (nextDestination !== previousDestination) {
            previousDestination = nextDestination;
            nextDestination = nextCell(previousDestination, direction);
        }

        // destination finale du robot
        destinationCell = nextDestination;
        if (originCell !== destinationCell) { // = robot can move in that direction
            moving = true;
            // notify people that we start moving - possibly add in the coordinates ...
            notifyRobotMoving(direction, color);
            animateRobotMove($robot, originCell, destinationCell, function(){
                notifyRobotMoved(direction, color);//notify that we are done moving ...
                moving = false;
            });

            while(!keepHistory && currentGame.moves.length > currentGame.undoIndex) {
                currentGame.moves.pop();
            }
            currentGame.moves.push({"movement":{"robot":color, "originRow":originCell.data("row"), "originColumn":originCell.data("column"), "direction": direction}});
            currentGame.undoIndex++;
            refreshMovementCounter();
            if (hasReachedObjective($robot, destinationCell)) {
                notifyGameSolved(currentGame.undoIndex); //undoIndex is the number of moves
            }
        }
    }
}

function animateRobotMove($robot, fromCell, toCell, doneMovingCallback){

    // current absolute position ?
    var originalPos = fromCell.offset();
    var origTop = originalPos.top;
    var origLeft = originalPos.left;

    // destination absolute position
    var finalPos = toCell.offset();
    var finalTop = finalPos.top;
    var finalLeft = finalPos.left;

    // on le sort de la cellule et on le met dans un coin du DOM
    $robot.appendTo("body");
    // mais on l'affiche au même endroit dans la page (absolute avec même offsets)
    $robot.css({'left':origLeft + 'px', 'top':origTop + 'px', position:'absolute'});
    // transition de l'un à l'autre
    $robot.animate({
                       left:finalLeft,
                       top:finalTop
                   }, 100, /* 'fast' = 200 ms, 'slow' = 600ms */
                   function() {
                       // Animation complete : remettre le robot dans la cellule de destination
                       $(this).css({left:'0px', top:'0px'}).appendTo(toCell.children().first()).offset(0, 0);
                       doneMovingCallback(); // call back : we are done moving
                   });
}

// Robot moves undo
function undo() {
    if (currentGame.undoIndex > 0) {
        if (!moving) {
            var newIndex = currentGame.undoIndex - 1;
            var move = currentGame.moves[newIndex].movement;
            var $robot = $("td .robot." + move.robot);
            if ($robot.length == 0) {
                // houston, guess what we got...
                console.log("no robot found while undoing a move, expected to find and select " + move.robot);
                return;
            }
            selectByColor(move.robot); // make it visibly selected
            var originCell = $robot.closest("td.cell");
            if (originCell.length > 0) {
                moving = true;
                currentGame.undoIndex = newIndex;
                refreshMovementCounter();
                originCell = originCell.first();
                var previousDestination = originCell;

                var indexOfOppositeDirection = (DIRECTIONS.indexOf(move.direction) +2) % 4;
                var oppositeDirection = DIRECTIONS[indexOfOppositeDirection];
                var nextDestination = nextCell(previousDestination, oppositeDirection);
                while (nextDestination.data("row") != move.originRow || nextDestination.data("column") != move.originColumn) {
                    previousDestination = nextDestination;
                    nextDestination = nextCell(previousDestination, oppositeDirection);
                }

                // destination finale du robot
                destinationCell = nextDestination;

                $robot.appendTo(destinationCell.children().first());
                moving = false;
            }
        }
    }
}

// Robot moves redo
function redo() {
    if(currentGame.moves.length > currentGame.undoIndex) {
        if (!moving) {
            var move = currentGame.moves[currentGame.undoIndex].movement;
            var $robot = $("td .robot." + move.robot);
            if ($robot.length == 0) {
                // houston, guess what we got...
                console.log("no robot found while undoing a move, expected to find and select " + move.robot);
                return;
            }
            selectByColor(move.robot); // make it visibly selected
            var originCell = $robot.closest("td.cell");
            if (originCell.length > 0) {
                moving = true;
                currentGame.undoIndex++;
                refreshMovementCounter();
                originCell = originCell.first();
                var previousDestination = originCell;
                var nextDestination = nextCell(previousDestination, move.direction);
                while (nextDestination !== previousDestination) {
                    previousDestination = nextDestination;
                    nextDestination = nextCell(previousDestination, move.direction);
                }

                // destination finale du robot
                destinationCell = nextDestination;

                $robot.appendTo(destinationCell.children().first());
                moving = false;
            }
        }
    }
}

function hasReachedObjective(robot, td) {
    // special id is put on the robot on server side
    var isRobotForObjective = $(robot).is("#robotForObjective");
    return isRobotForObjective && $(td).find("#objective").length > 0;
}


// Board methods
// -------------

// tells us where the robot will end up if we try moving it from cell "td" in direction "direction"
// returns itself if the robot cannot move in that direction
// direction is one of the strings from DIRECTIONS
function nextCell(td, direction) {
    var nextCell = null;
    switch (direction) {
        case DIRECTION_UP:
            if (!td.is(".wall-top")) {
                var $td = $(td);
                var col = $td.closest("tr").children().index($td);
                nextCell = $td.closest("tr").prev().children()[col];
                nextCell = $(nextCell);
            }
            break;
        case DIRECTION_DOWN:
            if (!td.is(".wall-bottom")) {
                var column = td.closest("tr").children().index(td);
                nextCell = td.closest("tr").next().children()[column];
                nextCell = $(nextCell);
            }
            break;
        case DIRECTION_LEFT:
            if (!td.is(".wall-left")) {
                nextCell = td.prev();
            }
            break;
        case DIRECTION_RIGHT:
            if (!td.is(".wall-right")) {
                nextCell = td.next();
            }
            break;
    }
    if (nextCell != null && !hasRobot(nextCell)) {
        return nextCell
    } else {
        return td;
    }
}

function hasRobot(td) {
    return $(td).find(".robot").length > 0;
}

function resetBoard() {
    while(currentGame.undoIndex > 0) {
        undo();
    }
    refreshMovementCounter();
    currentGame.moves = new Array();
    selectRobotOfObjective();
}


// User input events (keyboards + visual keys)
// -----------------

// mapping key Code => function to call
var KEY_ACTION_MAPPINGS = {};
// up
KEY_ACTION_MAPPINGS[KEY_ARROW_UP] = function(){requestSelectedRobotMovement(DIRECTION_UP);};
KEY_ACTION_MAPPINGS[KEY_LETTER_I] = function(){requestSelectedRobotMovement(DIRECTION_UP);};
// down
KEY_ACTION_MAPPINGS[KEY_ARROW_DOWN] = function(){requestSelectedRobotMovement(DIRECTION_DOWN);};
KEY_ACTION_MAPPINGS[KEY_LETTER_K] = function(){requestSelectedRobotMovement(DIRECTION_DOWN);};
// left
KEY_ACTION_MAPPINGS[KEY_ARROW_LEFT] = function(){requestSelectedRobotMovement(DIRECTION_LEFT);};
KEY_ACTION_MAPPINGS[KEY_LETTER_J] = function(){requestSelectedRobotMovement(DIRECTION_LEFT);};
// right
KEY_ACTION_MAPPINGS[KEY_ARROW_RIGHT] = function(){requestSelectedRobotMovement(DIRECTION_RIGHT);};
KEY_ACTION_MAPPINGS[KEY_LETTER_L] = function(){requestSelectedRobotMovement(DIRECTION_RIGHT);};
// next / previous
KEY_ACTION_MAPPINGS[KEY_LETTER_D] =  selectNextRobot;
KEY_ACTION_MAPPINGS[KEY_LETTER_S] =  selectPreviousRobot;
// undo / redo
KEY_ACTION_MAPPINGS[KEY_LETTER_C] =  redo;
KEY_ACTION_MAPPINGS[KEY_LETTER_X] =  undo;


// setup key handlers and click handlers related to the game (moving robots etc)
function setUpGameControlHandlers(){
    // real keyboard
    $window.keydown(function(event){
        //console.debug(event.keyCode);
        if(event.keyCode in KEY_ACTION_MAPPINGS){
            KEY_ACTION_MAPPINGS[event.keyCode].apply();
        }
    });

    // on-screen keyboard
    // mapping the id of the key on screen to the function to call
    var virtualKeyboardActions = {
        "#key-up"  : function(){requestSelectedRobotMovement(DIRECTION_UP);},
        "#key-left": function(){requestSelectedRobotMovement(DIRECTION_LEFT);},
        "#key-down": function(){requestSelectedRobotMovement(DIRECTION_DOWN);},
        "#key-right": function(){requestSelectedRobotMovement(DIRECTION_RIGHT);},
        "#key-next":selectNextRobot,
        "#key-prev":selectPreviousRobot,
        "#key-undo":undo,
        "#key-redo":redo
    };
    $.each(virtualKeyboardActions, function (keyboardKeyId, functionToCall){
        $(keyboardKeyId).click(functionToCall);
    });

    // select a robot by clicking it (unselects the other selected robot)
    $("td .robot").click(function(){
        var $this = $(this);
        if (!$this.is(".selected")) {
            $(".selected").toggleClass("selected");
        }
        $(this).toggleClass("selected");
    });

    // robot from header selects the robot on the board
    $("a#headerRobot").click(function (e) {
        selectRobotOfObjective();
        e.preventDefault();
    });

    // make the objective more obvious when clicking in the header
    $("a#headerGoal span.symbol").click(function(){
        $("div.symbol:not(#objective)").toggleClass("transparent");
    });
}



// the loop in charge of updating the time left and the progress bar (disconnected from server polling loop)
function doClientRefreshLoop(){
    var duration = currentGame.duration;

    // decrease the "time left" stuff ...
    var timeLeft = currentGame.secondsLeft;
    timeLeft = timeLeft - (REFRESH_LOOP_REPEAT_TIME / 1000);
    currentGame.secondsLeft = timeLeft;

    // update the progress bar : width and color.
    refreshProgressBar();
    // refresh the timer
    refreshTimer();

    if(timeLeft <= 0){
        notifyGameTimeUp();
    }
    else
    {
        // do it again
        if(currentGame.gameIsOn){
            // no need to keep on refreshing when game is over ...
            setTimeout(doClientRefreshLoop, REFRESH_LOOP_REPEAT_TIME);
        }
    }
}

// update the size and color of the progress bar depending on how much time is left
function refreshProgressBar(){
    var percentLeft = 100 * ( currentGame.secondsLeft / currentGame.duration);
    var $progressBarContainer = $("#progressBarContainer");

    // remove color classes from container, if any
    $progressBarContainer.removeClass("progress-info")
        .removeClass("progress-success")
        .removeClass("progress-warning")
        .removeClass("progress-danger");

    if(percentLeft < 10 || currentGame.gamePhase == PHASEID_GAME_2) {
        $progressBarContainer.addClass("progress-danger"); // red
    }
    else if(percentLeft < 25){
        $progressBarContainer.addClass("progress-warning"); // yellow
    }
    else if(percentLeft < 50){
        $progressBarContainer.addClass("progress-info"); //blue
    }
    else{
        $progressBarContainer.addClass("progress-success"); //green
    }

    var $progressBar = $('#progressBar') ;
    $progressBar.css('width', percentLeft + '%');
}

function refreshTimer(){
    var $timeLeft = $("a#timeLeft");
    $timeLeft.text(Math.ceil(currentGame.secondsLeft));
}

function triggerTimeAttack(){
    var $mainContainer = $("#container");
    $mainContainer.prepend('<div class="alert alert-danger" data-dismiss="alert"><a title="close" class="close">&times;</a>' + $_("game.solutionfound") + "</div>");
}

function activateNextGameLink() {
    var $endOfGameModalFooter = $("#endOfGameModalFooter");
    var $waitingForGame = $("#waitingForGame");
    $waitingForGame.remove();
    $endOfGameModalFooter.append('<a id="joinNextGame" class="btn btn-success" href="' + jsRoutes.controllers.Gaming.currentGame(currentGame.roomId).url + '" >' + $_("game.nextgame") + '</a>');
}

function serverRefresh(continuation) {
    console.log("syncing time with server");
    $.ajax({
        url: jsRoutes.controllers.Gaming.gameStatus(currentGame.roomId, currentGame.gameId).url,
        success:function (data) {
            // resync the time left
            currentGame.secondsLeft = data.game.timeLeft;
            currentGame.duration = data.game.duration;
            if (data.game.timeLeft > 0) {
                if(continuation) {
                    continuation();
                }
            }
        },
        statusCode:{
            410:function () {
                //alert("The game you asked is finished .... ");
                notifyGameTimeUp();
            }
        }
    });

}

function doServerRefreshLoop() {
    function continuation() {
        if(currentGame.gameIsOn){
            // refresh at the end of the time left
            // close refresh near the end ... but not more often than SERVER_POLLING_REPEAT_TIME_MIN
            var refreshTime = Math.max( (currentGame.secondsLeft * 1000) / 2, SERVER_POLLING_REPEAT_TIME_MIN);
            setTimeout(doServerRefreshLoop, refreshTime);
        }
    }
    serverRefresh(continuation)
}


///////////////////// WEB SOCKETS ////////////////////

// WS CONSTANTS - MESSAGE IDs

var PHASEID_GAME_1 = "GAME_1"
var PHASEID_GAME_2 = "GAME_2"
var PHASEID_SHOW_SOLUTION = "SHOW_SOLUTION"

var MSGID_USER_REFRESH = "USER_REFRESH" // used to notify joining and leaving players
var MSGID_SOLUTION_FOUND = "SOLUTION_FOUND" // used to trigger game phase 2 or just update best move if already in phase 2
var MSGID_TIME_UP = "TIME_UP" // time's up, round ends
var MSGID_NEW_ROUND = "NEW_ROUND" // a new round starts with a new board

function connectPlayer() {
    if (currentGame.gameSocket === null) {
        var urlBase = window.location.href.substr("http://".length);
        urlBase = urlBase.substr(0, urlBase.indexOf('/'));
        // relativeUrl for connection for player
        var relativeUrl = jsRoutes.controllers.Gaming.connectPlayer(currentGame.roomId, currentGame.playerName).url; // starts with /)
        currentGame.gameSocket = new WebSocket("ws://" + urlBase + relativeUrl);
        currentGame.gameSocket.onopen = function(e) {refreshScores();}; // refresh the scores when we have opened the connection
        currentGame.gameSocket.onmessage = messageReceived;
        // TODO : handle socket closing gracefully
        //currentGame.gameSocket.onclose = ...
        // TODO: ws error management
        //currentGame.gameSocket.onerror = ...
    }
}

var messageReceived = function(event){
    console.debug("SERVER>" + event.data);
    var d = JSON.parse(event.data); // parse it
    
    var type = d.type;
    if (type === MSGID_USER_REFRESH){
        console.log("in phase " + currentGame.gamePhase + " users changed");
        refreshScores();
    } else if(type === MSGID_SOLUTION_FOUND){
        if(currentGame.gamePhase == PHASEID_GAME_1){
            currentGame.gamePhase = PHASEID_GAME_2;
            triggerTimeAttack();
            console.log("entered phase " + currentGame.gamePhase);
        }
        console.log("new best solution found");
        serverRefresh();
        refreshScores();
    } else if(type === MSGID_TIME_UP){
        // should switch to display solution screen, merge with refresh loop
        if(currentGame.gamePhase != PHASEID_SHOW_SOLUTION){
            currentGame.gamePhase = PHASEID_SHOW_SOLUTION
            console.log("entered phase " + currentGame.gamePhase);
            notifyGameTimeUp();
        } else {
            activateNextGameLink();
            notifyGameTimeUp();
        }
    } else if(type === MSGID_NEW_ROUND){
        // should propose to join new game, merge with refresh loop
        console.log("new game ready");
    } else if(type === "player.kickout"){
        currentGame.gameIsOn = false; // not playing .. stop the refreshing and all the bazar ...
        currentGame.gameSocket.close();
        alert($_("game.kicked") + " " + d.args[0]);
        // redirect home ??
    }else{
        // unsupported message
        console.log("unsupported message received with type " + type)
    }
}

function refreshScores(){
    jsRoutes.controllers.Gaming.gameScores(currentGame.roomId, currentGame.gameId).ajax({
        cache:false,
        success: function(data, textStatus, jqXHR){
            //refill the leaderboard table
            var $tbody = $("tbody#leaderBoard");
            $tbody.empty();
            $.each(data.scores, function(index, playerScore){
                $tbody.append("<tr><th>" + playerScore.player +  "</th><td><span>"+ playerScore.score + "</span></td></tr>");
            });
        }
    });
}

// submit the current score ...
// provide call back for what to do it :
// - submission succeeds
// - submission fails
// - done submitting and got the reply (after success and failure)
function submitSolution(successCallback, failureCallback, completedCallback) {
    // how to handle submission of a game that is finished ?
    // the server expects moves to be a list of strings, instead of a list of proper objects ...
    // jsonify it before, but this can be improved (do not handle as string on server side .. parse it via Json tools)
    var movesToSend = $.map( currentGame.moves.slice(0, currentGame.undoIndex), function(val, i){return JSON.stringify(val);});
    // hidden inputs somewhere in the page ... to improve
    var roomId = currentGame.roomId;
    var gameId = currentGame.gameId;
    jsRoutes.controllers.Gaming.submitSolution(roomId, gameId).ajax({
        data:{"solution":JSON.stringify({"moves":movesToSend})},
        statusCode: {
            202: function() { // Accepted (202)
              successCallback();
            },
            406: function(){  // NotAcceptable (406)
              failureCallback("Solution not accepted - did you cheat ?");
            },
            410: function(){ // Gone (410)
              failureCallback("Too late, the game is over");
            }
        },
        complete: function(){
          completedCallback();
        }
    });
}

  // Exports
  // ==========================

  // makes public members public
  return {
    "init": init,
    "resetBoard": resetBoard,
    "submitSolution" : submitSolution,
    "currentState" : currentState //convenient to debug ... in console : JSON.stringify( robotagram.game.currentState())
  }
})(jQuery);