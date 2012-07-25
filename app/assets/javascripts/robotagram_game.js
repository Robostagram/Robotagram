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
var SERVER_POLLING_REPEAT_TIME = 3000; /*refresh loop to poll the server for status update*/

// Keyboard
// --------
// movements
var KEY_MOVE_UP = 105;     // 105: I : up
var KEY_MOVE_LEFT = 106;   // 106: J : left
var KEY_MOVE_DOWN = 107;   // 107: K : down
var KEY_MOVE_RIGHT = 108;  // 108: L : right
// undo/redo
var KEY_UNDO = 120;             // 120: X : undo
var KEY_REDO = 99;              // 99 : C : redo
// select another robot
var KEY_SELECT_PREVIOUS = 115;  // 115: S : previous
var KEY_SELECT_NEXT = 100;      // 100: D : next




// ---------------
// PRIVATE METHODS
// ---------------

// Game dynamics
// -------------

var gameIsOn = false; // is there a game currently being played ?

// the entry point for the game loop
function init(){
    // prepare the board
    initializeBoard();

    // init the websocket stuff ...
    connectPlayer();
    gameIsOn = true;

    // when leaving the window, disconnect the user
    window.onbeforeunload = function() {
        // the game is finished ... just disconnect
        if(!gameIsOn){ // no game is playing ... no need to ask confirmation before leaving
            return
        }
        // should probably ask confirmation to user ??
        return $_("game.leave.gameIsActive.confirm");
    };

    // launch the client-site countdown (frequent updates)
    doClientRefreshLoop();
    // launch the server polling to resync timer and game status (less frequent)
    doServerRefreshLoop();

    // set up actions on game events (time up, solution found, move robot etc)
    bindGameEventHandlers();

    // make keyboard and mouse trigger the events ...
    setUpGameControlHandlers();
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
        gameIsOn = false; // it's ok to leave the page, now
        $("#endOfGameModal").modal('show');
        $("#winModal").modal('hide');
    });

    $window.on(EVENT_GAME_SOLVED, function(e, numberOfMoves){
        //console.debug(e.type, e.namespace, numberOfMoves);
        $("#winModal").modal('show');
    });
}

function displayMoveCounter(nbMoves){
    $("#currentScore").text(nbMoves + "");
}


// Robot selection
// ---------------

// get the index of the selected robot in the ROBOT_COLORS array
// returns 0 if no robot is selected
function getIndexOfCurrentlySelectedRobot() {
    var $currentSelected = $(".robot.selected");
    var curColorIndex = 0; //blue by default
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
    curColorIndex += 1;
    if (curColorIndex >= ROBOT_COLORS.length) {
        curColorIndex = 0;
    }
    selectByColor(ROBOT_COLORS[curColorIndex]);
}

// select the previous robot (order defined by ROBOT_COLORS)
function selectPreviousRobot() {
    var curColorIndex = getIndexOfCurrentlySelectedRobot();
    curColorIndex -= 1;
    if (curColorIndex < 0) {
        curColorIndex = ROBOT_COLORS.length - 1;
    }
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
    var color = ROBOT_COLORS[getIndexOfCurrentlySelectedRobot()];
    requestRobotMove(direction, color);
}

/* stack of moves for undo/redo ...*/
var moves = new Array();
var undoIndex = 0;
var moving = false;

// move the robot of a given color in the requested direction
// the direction is one of the strings from DIRECTIONS
function moveRobot(color, direction, keepHistory) {
    var $robot = $("td .robot." + color), originCell, destinationCell = null, previousDestination, nextDestination;
    if ($robot.length == 0) {
        // pas de robot
        alert("You must select a robot");
        return;
    }

    originCell = $robot.closest("td.cell");
    // seulement si le robot est bien dans une cellule, pas en déplacement
    if (!moving && originCell.length > 0) {
        moving = true;
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
            // notify people that we start moving - possibly add in the coordinates ...
            notifyRobotMoving(direction, color);
            // current absolute position ?
            var originalPos = originCell.offset();
            var origTop = originalPos.top;
            var origLeft = originalPos.left;

            // destination absolute position
            var finalPos = destinationCell.offset();
            var finalTop = finalPos.top;
            var finalLeft = finalPos.left;

            // on le sort de la cellule
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
                               $(this).css({left:'0px', top:'0px'}).appendTo(destinationCell.children().first()).offset(0, 0);
                               notifyRobotMoved(direction, color);//notify that we are done moving ...
                           });
            while(!keepHistory && moves.length > undoIndex) {
                moves.pop();
            }
            moves.push({"movement":{"robot":color, "originRow":originCell.data("row"), "originColumn":originCell.data("column"), "direction": direction}});
            undoIndex++;
            displayMoveCounter(undoIndex);
            if (hasReachedObjective($robot, destinationCell)) {
                notifyGameSolved(undoIndex); //undoIndex is the number of moves
            }
        }
        moving = false;
    }
}

// Robot moves undo
function undo() {
    if (undoIndex > 0) {
        if (!moving) {
            var newIndex = undoIndex - 1;
            var move = moves[newIndex].movement;
            var $robot = $("td .robot." + move.robot);
            selectByColor(move.robot); // make it visibly selected
            if ($robot.length == 0) {
                // houston, guess what we got...
                alert("kein robot!");
            }
            var originCell = $robot.closest("td.cell");
            if (originCell.length > 0) {
                moving = true;
                undoIndex = newIndex;
                displayMoveCounter(undoIndex);
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
    if(moves.length > undoIndex) {
        if (!moving) {
            var move = moves[undoIndex].movement;
            var $robot = $("td .robot." + move.robot);
            selectByColor(move.robot); // make it visibly selected
            if ($robot.length == 0) {
                // houston, guess what we got...
                alert("kein robot!");
            }
            var originCell = $robot.closest("td.cell");
            if (originCell.length > 0) {
                moving = true;
                displayMoveCounter(++undoIndex);
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

function initializeBoard(){
    //select the robot corresponding to the objective on page load
    selectRobotOfObjective();
}

function resetBoard() {
    while(undoIndex > 0) {
        undo();
    }
    moves = new Array();
    selectRobotOfObjective();
    displayMoveCounter(0);
}


// User input events (keyboards + visual keys)
// -----------------

// mapping key Code => function to call
var KEY_ACTION_MAPPINGS = {};
KEY_ACTION_MAPPINGS[KEY_MOVE_UP] = function(){requestSelectedRobotMovement(DIRECTION_UP);};
KEY_ACTION_MAPPINGS[KEY_MOVE_DOWN] = function(){requestSelectedRobotMovement(DIRECTION_DOWN);};
KEY_ACTION_MAPPINGS[KEY_MOVE_LEFT] = function(){requestSelectedRobotMovement(DIRECTION_LEFT);};
KEY_ACTION_MAPPINGS[KEY_MOVE_RIGHT] = function(){requestSelectedRobotMovement(DIRECTION_RIGHT);};
KEY_ACTION_MAPPINGS[KEY_SELECT_NEXT] =  selectNextRobot;
KEY_ACTION_MAPPINGS[KEY_SELECT_PREVIOUS] =  selectPreviousRobot;
KEY_ACTION_MAPPINGS[KEY_REDO] =  redo;
KEY_ACTION_MAPPINGS[KEY_UNDO] =  undo;


// setup key handlers and click handlers related to the game (moving robots etc)
function setUpGameControlHandlers(){
    // real keyboard
    $window.keypress(function(event){
        if(event.which in KEY_ACTION_MAPPINGS){
            KEY_ACTION_MAPPINGS[event.which].apply();
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


// store the actual time left (as double, with details and stuff )
// resync'd with server on a regular basis (pollTimer)
var previousTimeLeft = -999;
// the loop in charge of updating the time left and the progress bar (disconnected from server polling loop)
function doClientRefreshLoop(){
    var duration = parseInt($("#gameDuration").val(), 10); //10 to parse as number in base 10 ...

    if(previousTimeLeft === -999){ // first time we display the progress bar, fill with full duration (hidden field in game)
        var timeLeftWhenPageWasLoaded = parseInt($("#secondsLeftOnPageLoad").val(), 10); //10 to parse as number in base 10 ...
        previousTimeLeft = timeLeftWhenPageWasLoaded;
    }
    // decrease the "time left" stuff ...
    var timeLeft = previousTimeLeft;
    timeLeft = timeLeft - (REFRESH_LOOP_REPEAT_TIME / 1000);
    previousTimeLeft = timeLeft;

    // and compute percentage left
    var percentLeft = 100 * ( timeLeft / duration);

    // update the progress bar : width and color.
    updateProgressBar(percentLeft);
    updateCountDown(timeLeft);

    if(timeLeft <= 0){
        notifyGameTimeUp();
    }
    else
    {
        // do it again
        if(gameIsOn){
            // no need to keep on refreshing when game is over ...
            setTimeout(doClientRefreshLoop, REFRESH_LOOP_REPEAT_TIME);
        }
    }
}

// update the size and color of the progress bar depending on how much time is left
function updateProgressBar(percentLeft){
    var $progressBarContainer = $("#progressBarContainer");

    // remove color classes from container, if any
    $progressBarContainer.removeClass("progress-info")
        .removeClass("progress-success")
        .removeClass("progress-warning")
        .removeClass("progress-danger");

    if(percentLeft < 10) {
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

function updateCountDown(timeLeft){
    var $timeLeft = $("a#timeLeft");
    $timeLeft.text(Math.ceil(timeLeft));
}

function doServerRefreshLoop() {
    $.ajax({
        url: jsRoutes.controllers.Gaming.gameStatus($("#roomId").val(), $("#gameId").val()).url,
        success:function (data) {
            // resync the time left
            previousTimeLeft = data.game.timeLeft;
            if (data.game.percentageDone <= 0) {
                notifyGameTimeUp();
            }
            else {
                if(gameIsOn){
                    setTimeout(doServerRefreshLoop, SERVER_POLLING_REPEAT_TIME);
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


///////////////////// WEB SOCKETS ////////////////////

var gameSocket = null;

function connectPlayer() {
    if (gameSocket === null) {
        var urlBase = window.location.href.substr("http://".length);
        urlBase = urlBase.substr(0, urlBase.indexOf('/'));
        // relativeUrl for connection for player
        var relativeUrl = jsRoutes.controllers.Gaming.connectPlayer($("#roomId").val(), $("#userName").text()).url; // starts with /)
        gameSocket = new WebSocket("ws://" + urlBase + relativeUrl);
        gameSocket.onopen = function(e) {refreshScores();}; // refresh the scores when we have opened the connection
        gameSocket.onmessage = messageReceived;
        // TODO : handle socket closing gracefully
        //gameSocket.onclose = ...
        // TODO: ws error managemenr
        //gameSocket.onerror = ...
    }
}



var messageReceived = function(event){
    console.debug("SERVER>" + event.data);
    var d = JSON.parse(event.data); // parse it

    if(d.type === "player.kickout"){
        gameIsOn = false; // not playing .. stop the refreshing and all the bazar ...
        gameSocket.close();
        alert("You have been kicked on in this window : " + d.args[0]);
        // redirect home ??
    }else{
        // whenever we get a message, refresh the scores ...
        refreshScores();
    }
}

function refreshScores(){
    jsRoutes.controllers.Gaming.gameScores($("#roomId").val(), $("#gameId").val()).ajax({
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
function sendScore(successCallback, failureCallback, completedCallback) {
    // how to handle submission of a game that is finished ?
    // the server expects moves to be a list of strings, instead of a list of proper objects ...
    // jsonify it before, but this can be improved (do not handle as string on server side .. parse it via Json tools)
    var movesToSend = $.map( moves.slice(0, undoIndex), function(val, i){return JSON.stringify(val);});
    // hidden inputs somewhere in the page ... to improve
    var roomId = $("#roomId").val();
    var gameId = $("#gameId").val();
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
    "sendScore" : sendScore
  }
})(jQuery);



