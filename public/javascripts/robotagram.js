// keyboard handler for robots moves
function keypressHandler(event) {
    if (DIRECTION_UP <= event.which && event.which <= DIRECTION_RIGHT) {
        moveRobot(event.which);
    }
    if (event.which === SELECT_NEXT) {
        selectNextRobot();
    }
    if (event.which === SELECT_PREVIOUS) {
        selectPreviousRobot();
    }
}

var ROBOT_COLORS = ['blue', 'red', 'yellow', 'green'];

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
    $(".selected").toggleClass("selected");
    $("td .robot." + colorName).toggleClass("selected");
}

/* called when clicking on the robot */
function robotClickHandler() {
    var $this = $(this);
    if (!$this.is(".selected")) {
        $(".selected").toggleClass("selected");
    }
    $(this).toggleClass("selected");
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

// ghost mode
var GHOST_MODE = 103;

// switch robot
// 115 s : previous
// 100 d : next
var SELECT_PREVIOUS = 115;
var SELECT_NEXT = 100;

/* global variable for current number of moves ...*/
//var moves = 0;
var moves = new Array();
var undoIndex = 0;

// move the robot in the requested direction
//
function moveRobot(direction) {
    var $robot = $(".robot.selected"), originCell, destinationCell = null, previousDestination, nextDestination;
    if ($robot.length == 0) {
        // pas de robot
        alert("You must select a robot");
        return;
    }

    originCell = $robot.parents("td.cell");
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
                           });
			moves.push(JSON.stringify({"movement":{"robot":getIndexOfCurrentlySelectedRobot(), "origin":{"row":originCell.data("row"), "column":originCell.data("column")}, "direction":direction}}));
            undoIndex++;
            $("#currentScore").text(undoIndex + "");
            if (hasReachedObjective($robot, destinationCell)) {

                window.onunload = null;// to prevent call to leaveGame() (see registration in javascript in initListeners())
                $("#winModal").modal('show');
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
                var col = $td.parents("tr").children().index($td);
                nextCell = $td.parents("tr").prev().children()[col];
                nextCell = $(nextCell);
            }
            break;
        case DIRECTION_DOWN:
            if (!td.is(".wall-bottom")) {
                var column = td.parents("tr").children().index(td);
                nextCell = td.parents("tr").next().children()[column];
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

function initListeners() {
    $(window).keypress(keypressHandler);
    $("td .robot").click(robotClickHandler);

    //on triche, et les touches affichées marchent comme un clavier
    $("#key-up").click(function() {
        moveRobot(DIRECTION_UP);
    });
    $("#key-down").click(function() {
        moveRobot(DIRECTION_DOWN);
    });
    $("#key-left").click(function() {
        moveRobot(DIRECTION_LEFT);
    });
    $("#key-right").click(function() {
        moveRobot(DIRECTION_RIGHT);
    });

    // robot from header selects the robot on the board
    $("a#headerRobot").click(function (e) {
        $(".selected").removeClass("selected");
        $("#robotForObjective").addClass("selected");
        e.preventDefault();
    });

    // info if you hover the objective
    $('#headerGoal span').popover({placement:'bottom', title:"Bring the robot here !"});
    var $robotOfObjective = $("#robotForObjective");
    $robotOfObjective.tooltip({title:"Bring this robot ...", trigger:'manual'});
    var $objective = $('#objective');
    $objective.tooltip({title:"... to this objective !",trigger:'manual'});

    // tooltip on robot on page load
    $robotOfObjective.tooltip('show');
    // but not for too long
    setTimeout(function(){$robotOfObjective.tooltip('hide');}, 3000);
    // tooltip on the objective a bit after the robot
    setTimeout(function(){$objective.tooltip('show');}, 1500);
    setTimeout(function(){$objective.tooltip('hide');}, 3000);


    connectPlayer();

    // when leaving the window, disconnect the user
    window.onunload = function() {
        // the game is finished ... just disconnect
        leaveGame(); //continue propagation of event

        // should probably ask confirmation to user ??
    };

    // launch the client-site countdown (frequent updates)
    // launch the server polling to resync timer and game status (less frequent)
    doRefreshLoop();
    reSyncGameStatusWithServer();
}

// function called when end of game is reached
var endOfGameNotified = false;
function notifyEndOfGame(){
    if(!endOfGameNotified){
        endOfGameNotified = true;
        $("#endOfGameModal").modal('show');
    }
}

// store the actual time left (as double, with details and stuff )
// resync'd with server on a regular basis (pollTimer)
var previousTimeLeft = -999;
var REFRESH_LOOP_REPEAT_TIME = 300; /* refresh time on client side . */
var SERVER_POLLING_REPEAT_TIME = 3000;

// the loop in charge of updating the time left and the progress bar (disconnected from server polling loop)
function doRefreshLoop(){
    var duration = parseInt($("#gameDuration").val(), 10);
    if(previousTimeLeft === -999){
        previousTimeLeft = duration;
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
        notifyEndOfGame();
    }
    else
    {
        // do it again
        setTimeout(doRefreshLoop, REFRESH_LOOP_REPEAT_TIME);
    }
}

function reSyncGameStatusWithServer() {
    $.ajax({
        url:document.URL + '/status',
        success:function (data) {
            // resync the time left
            previousTimeLeft = data.game.timeLeft;
            if (data.game.percentageDone <= 0) {
                notifyEndOfGame();
            }
            else {
                setTimeout(reSyncGameStatusWithServer, SERVER_POLLING_REPEAT_TIME);
            }
        },
        statusCode:{
            410:function () {
                alert("The game you asked is finished .... ");
                notifyEndOfGame();
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
            gameSocket = new WebSocket("ws://" + urlBase + "/connectPlayer/" + $("#userName").text());
            gameSocket.onmessage = receiveSummary;
        }
    }

    function leaveGame() {
        var message = JSON.stringify({"leave":{"player":$("#userName").text()}});
        gameSocket.send(message);
    }

    function sendScore() {
        // how to handle submission of a game that is finished ?
        var message = JSON.stringify({"solution":{"player":$("#userName").text(), "moves":moves}});
        gameSocket.send(message);
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