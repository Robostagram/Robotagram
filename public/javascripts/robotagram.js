var REQUEST_ROBOT_MOVE = "requestMove.robot.robotagram";

var EVENT_ROBOT_MOVING = "moving.robot.robotagram";
var EVENT_ROBOT_MOVED = "moved.robot.robotagram";

var EVENT_GAME_TIMEUP = "timeUp.game.robotagram";
var EVENT_GAME_SOLVED = "solved.game.robotagram";

function requestSelectedRobotMovement(direction_keyboard_code){
    var color = ROBOT_COLORS[getIndexOfCurrentlySelectedRobot()];
    var direction = directionCodeToString(direction_keyboard_code);
    var $robotToMove = $("td .robot." + color);
    $robotToMove.trigger(REQUEST_ROBOT_MOVE, [direction, color]);
}

//user friendly version of a direction (Up, Down etc ...)
function directionCodeToString(direction_keyboard_code){
    return DIRECTIONS[direction_keyboard_code - MAGIC_NUMBER];
}

// the key code coming from a user friendly name of direction
function directionStringToCode(directionName){
    return MAGIC_NUMBER + (DIRECTIONS.indexOf(directionName)) % 4;
}

// keyboard handler for robots moves
function keypressHandler(event) {
    if (DIRECTION_UP <= event.which && event.which <= DIRECTION_RIGHT) {
        requestSelectedRobotMovement(event.which);
    }
    if (event.which === SELECT_NEXT) {
        selectNextRobot();
    }
    if (event.which === SELECT_PREVIOUS) {
        selectPreviousRobot();
    }
    if (event.which === REDO) {
        redo();
    }
    if (event.which === UNDO) {
        undo();
    }
}

var ROBOT_COLORS = ['Blue', 'Red', 'Yellow', 'Green'];

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

// select the next robot (order defined by ROBOT_COLORS
function selectNextRobot() {
    var curColorIndex = getIndexOfCurrentlySelectedRobot();
    curColorIndex += 1;
    if (curColorIndex >= ROBOT_COLORS.length) {
        curColorIndex = 0;
    }
    selectByColor(ROBOT_COLORS[curColorIndex]);
}

// select the previous robot (order defined by ROBOT_COLORS
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

// direction:
// 105: i : up
// 106: j : left
// 107: k : down
// 108: l : right
var DIRECTION_UP = 105;
var DIRECTION_LEFT = 106;
var DIRECTION_DOWN = 107;
var DIRECTION_RIGHT = 108;

var DIRECTIONS = ['Up', 'Left', 'Down', 'Right'];
var MAGIC_NUMBER = 105; //substract this to a direction -> its index in DIRECTIONS

// 120: x : undo
// 99: c : redo
var UNDO = 120;
var REDO = 99;

// switch robot
// 115 s : previous
// 100 d : next
var SELECT_PREVIOUS = 115;
var SELECT_NEXT = 100;

/* stack of moves for undo/redo ...*/
var moves = new Array();
var undoIndex = 0;

var moving = false;

// move the robot of a given color in the requested direction
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
            $robot.trigger(EVENT_ROBOT_MOVING, [directionCodeToString(direction), color]);
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
                               $(this).trigger(EVENT_ROBOT_MOVED, [directionCodeToString(direction), color]); //notify that we ae done moving ...
                           });
            while(!keepHistory && moves.length > undoIndex) {
                moves.pop();
            }
            moves.push({"movement":{"robot":color, "originRow":originCell.data("row"), "originColumn":originCell.data("column"), "direction": directionCodeToString(direction)}});
            undoIndex++;
            $("#currentScore").text(undoIndex + "");
            if (hasReachedObjective($robot, destinationCell)) {
                $(window).trigger(EVENT_GAME_SOLVED, [undoIndex]); //undoIndex is the number of moves
            }
        }
        moving = false;
    }
}

function closeWinModal() {
    $("#winModal").modal('hide');
}

function resetBoard() {
    while(undoIndex > 0) {
        undo();
    }
    moves = new Array();
    $(".selected").removeClass("selected");
    $("#robotForObjective").addClass("selected");
    $("#currentScore").text(undoIndex + "");
}

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
                $("#currentScore").text(undoIndex + "");
                originCell = originCell.first();
                var previousDestination = originCell;
                var oppositeDirection = MAGIC_NUMBER + (DIRECTIONS.indexOf(move.direction) +2) % 4;
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
                $("#currentScore").text(++undoIndex + "");
                originCell = originCell.first();
                var previousDestination = originCell;
                var direction = MAGIC_NUMBER + DIRECTIONS.indexOf(move.direction);
                var nextDestination = nextCell(previousDestination, direction);
                while (nextDestination !== previousDestination) {
                    previousDestination = nextDestination;
                    nextDestination = nextCell(previousDestination, direction);
                }

                // destination finale du robot
                destinationCell = nextDestination;

                $robot.appendTo(destinationCell.children().first());
                moving = false;
            }
        }
    }
}

function hasRobot(td) {
    return $(td).find(".robot").length > 0;
}

function hasReachedObjective(robot, td) {
    // special id is put on the robot on server side
    var isRobotForObjective = $(robot).is("#robotForObjective");

    return isRobotForObjective && $(td).find("#objective").length > 0;
}

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

// setup key handlers and click handlers related to the game (moving robots etc)
function setUpGameControlHandlers(){

    $(window).keypress(keypressHandler);

    $("td .robot").click(function(){
        var $this = $(this);
        if (!$this.is(".selected")) {
            $(".selected").toggleClass("selected");
        }
        $(this).toggleClass("selected");
    });

    //on triche, et les touches affichées marchent comme un clavier
    $("#key-up").click(function() {
        requestSelectedRobotMovement(DIRECTION_UP);
    });
    $("#key-down").click(function() {
        requestSelectedRobotMovement(DIRECTION_DOWN);
    });
    $("#key-left").click(function() {
        requestSelectedRobotMovement(DIRECTION_LEFT);
    });
    $("#key-right").click(function() {
        requestSelectedRobotMovement(DIRECTION_RIGHT);
    });
    $("#key-next").click(function() {
        selectNextRobot();
    });
    $("#key-prev").click(function() {
        selectPreviousRobot();
    });
    $("#key-undo").click(function() {
        undo();
    });
    $("#key-redo").click(function() {
        redo();
    });

}

// set up the (useless) events that happen when clicking in the header
function setUpHeaderShortcuts(){
    // robot from header selects the robot on the board
    $("a#headerRobot").click(function (e) {
        $(".selected").removeClass("selected");
        $("#robotForObjective").addClass("selected");
        e.preventDefault();
    });

    // make the objective more obvious when hovering in the header
    $("a#headerGoal span.symbol").click(function(){
        $("div.symbol:not(#objective)").toggleClass("transparent");
    });
}

// set up the tooltip on current robot and target objective ... should be less intrusive
function setUpHelpAndTooltips(){
    var $robotOfObjective = $("#robotForObjective");

    $robotOfObjective.tooltip({
        title:$_("game.tooltip.bringThisRobot"),
        trigger:'manual',
        placement:function(){
            // are we on the first row ? then display the tooltip at the bottom ...
            var $cell = this.$element.closest("td.cell");
            if($cell.data('row')== "0"){
                return 'bottom';
            }
            return 'top';
        }
    });
    var $objective = $('#objective');
    $objective.tooltip({
        title:$_("game.tooltip.toThisObjective"),
        trigger:'manual',
        placement:function(){
            // are we on the first row ? then display the tooltip at the bottom ...
            var $cell = this.$element.closest("td.cell");
            if($cell.data('row')== "0"){
                return 'bottom';
            }
            return 'top';
        }
    });

    // tooltip on robot on page load
    $robotOfObjective.tooltip('show').effect('shake', { times:3, distance:5, direction: 'up' } , 200);
    // but not for too long
    setTimeout(function(){$robotOfObjective.tooltip('hide');}, 3000);
    // tooltip on the objective a bit after the robot
    setTimeout(function(){$objective.tooltip('show').effect('pulsate', { times:3 } , 400);}, 1500);
    setTimeout(function(){$objective.tooltip('hide');}, 3000);
}
var gameIsOn = false; // is there a game currently being played ?
function initListeners(){
    setUpGameControlHandlers();

    setUpHeaderShortcuts();

    setUpHelpAndTooltips();

    var $robotOfObjective = $("#robotForObjective");
    //select the robot correspounding to the objective on page load
    $robotOfObjective.addClass("selected");

    connectPlayer();

    gameIsOn = true;

    // when leaving the window, disconnect the user
    window.onbeforeunload = function() {
        // the game is finished ... just disconnect
        if(!gameIsOn){ // no game is playing ... no need to ask confirmation before leaving
            return
        }
        return $_("game.leave.gameIsActive.confirm")
        // should probably ask confirmation to user ??
    };

    // launch the client-site countdown (frequent updates)
    // launch the server polling to resync timer and game status (less frequent)
    doRefreshLoop();
    reSyncGameStatusWithServer();

    // game event listeners
    var $window = $(window);
    // events going on in the game ?
    $window.on(REQUEST_ROBOT_MOVE, function(e, direction, color){
        //console.debug(e.type, e.namespace, direction, color);
        moveRobot(color, directionStringToCode(direction));//keepHistory
    });

    /*$window.on(EVENT_ROBOT_MOVING, function(e, direction, color){
        console.debug(e.type, e.namespace, direction, color);
    });
    $window.on(EVENT_ROBOT_MOVED, function(e, direction, color){
        console.debug(e.type, e.namespace, direction, color);
    });*/

    // do it only once
    $window.one(EVENT_GAME_TIMEUP, function(e){
        //console.debug(e.type, e.namespace);
        gameIsOn = false; // it's ok to leave the page, now
        $("#endOfGameModal").modal('show');
        $("#winModal").modal('hide');
    });

    $window.on(EVENT_GAME_SOLVED, function(e, numberOfMoves){
        //console.debug(e.type, e.namespace, numberOfMoves);
        //win modal shown does not imply redirection
        //no need to deactivate leaveGame on unload
        //window.onunload = null;// to prevent call to leaveGame() (see registration in javascript in initListeners())
        $("#winModal").modal('show');
    });
}

// store the actual time left (as double, with details and stuff )
// resync'd with server on a regular basis (pollTimer)
var previousTimeLeft = -999;
var REFRESH_LOOP_REPEAT_TIME = 300; /* refresh time on client side . */
var SERVER_POLLING_REPEAT_TIME = 3000;

// the loop in charge of updating the time left and the progress bar (disconnected from server polling loop)
function doRefreshLoop(){
    var duration = parseInt($("#gameDuration").val(), 10);
    var timeLeftWhenPageWasLoaded = parseInt($("#secondsLeftOnPageLoad").val(), 10);
    if(previousTimeLeft === -999){ // first time we display the progress bar, fill with full duration (hidden field in game)
        previousTimeLeft = timeLeftWhenPageWasLoaded;
    }
    // decrease the "time left" stuff ...
    var timeLeft = previousTimeLeft;
    timeLeft = timeLeft - (REFRESH_LOOP_REPEAT_TIME / 1000);
    previousTimeLeft = timeLeft;

    // and compute percentage left
    var percentLeft = 100 * ( timeLeft / duration);
    //console.log(percentLeft);
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
    var $timeLeft = $("a#timeLeft");
    $timeLeft.text(Math.ceil(timeLeft));

    if(timeLeft <= 0){
        $(window).trigger(EVENT_GAME_TIMEUP);
    }
    else
    {
        // do it again
        setTimeout(doRefreshLoop, REFRESH_LOOP_REPEAT_TIME);
    }
}

function reSyncGameStatusWithServer() {
    $.ajax({
        url: jsRoutes.controllers.Gaming.gameStatus($("#roomId").val(), $("#gameId").val()).url,
        success:function (data) {
            // resync the time left
            previousTimeLeft = data.game.timeLeft;
            if (data.game.percentageDone <= 0) {
                $(window).trigger(EVENT_GAME_TIMEUP);
            }
            else {
                if(gameIsOn){
                    setTimeout(reSyncGameStatusWithServer, SERVER_POLLING_REPEAT_TIME);
                }
            }
        },
        statusCode:{
            410:function () {
                //alert("The game you asked is finished .... ");
                $(window).trigger(EVENT_GAME_TIMEUP);
            }
        }
    });
}

// shitty loading indicator to show something is happening ... putting it on body so far
function showLoading(){
    $("body").addClass("loading");
}
function hideLoading(){
    $("body").removeClass("loading");
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
            gameSocket.onmessage = messageReceived;
        }
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

    var messageReceived = function(event){
        console.debug("SERVER>" + event.data);
        var d = JSON.parse(event.data); // parse it

        if(d.type === "player.kickout"){
            gameIsOn = false; // not playing .. stop the refreshing and all the bazar ...
            alert("You have been kicked on in this window : " + d.args[0]);
            // redirect home ??
        }
    }

    var receiveSummary = function(event) {
        var summary = JSON.parse(event.data);
        // Handle scores
        var scores = summary.scores;
        $('#leaderBoard').empty(); // empty lines to ensure remove of players that left
        for (var i = 0; i < scores.length; i++) {
            var playerName = scores[i].player;
            var score = scores[i].score;
            if (score < 1) {
                score = '-';
            }

            var id = "score_" + playerName;
            if ($("#" + id).length > 0) {
                $("#best_" + id).text(score);
            } else {
                $('#leaderBoard').append('<tr id="' + id + '"><th>' + playerName + '</th><td><span id="best_score_' + playerName + '">' + score
                    + '</span></td></tr>');
            }
        }
    };