function keypressHandler(event) {
    if (DIRECTION_UP <= event.which && event.which <= DIRECTION_RIGHT && $(".selected").hasClass('robot')) {
        moveRobot(event.which);
    }
    if (event.which === SELECT_NEXT) {
        selectNextRobot();
    }
    if (event.which === SELECT_PREVIOUS) {
        selectPreviousRobot();
    }
}

var ROBOT_COLORS = ['blue', 'red', 'yellow', 'green']


function getIndexOfCurrentlySelectedRobot() {
    var $currentSelected = $(".robot.selected");
    var curColorIndex = 0; //blue by default
    if ($currentSelected.length > 0) {
        $.each(ROBOT_COLORS, function (index, colorName) {
            if ($currentSelected.hasClass(colorName)) {
                curColorIndex = index;
                return false; //stop here !
            }
            return true; //continue
        });
    }
    return curColorIndex
}

function selectNextRobot() {
    var curColorIndex = getIndexOfCurrentlySelectedRobot();
    curColorIndex += 1;
    if (curColorIndex >= ROBOT_COLORS.length) {
        curColorIndex = 0;
    }

    selectByColor(ROBOT_COLORS[curColorIndex]);
}

function selectPreviousRobot() {
    var curColorIndex = getIndexOfCurrentlySelectedRobot();

    curColorIndex -= 1;
    if (curColorIndex < 0) {
        curColorIndex = ROBOT_COLORS.length - 1;
    }

    selectByColor(ROBOT_COLORS[curColorIndex]);
}

/* mark the robot with given color as selected */
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


// switch robot
// 115 s : previous
// 100 d : next
var SELECT_PREVIOUS = 115;
var SELECT_NEXT = 100;

/* global variable for current number of moves ...*/
var moves = 0;


function moveRobot(direction) {
    var $robot = $(".selected"),
        originCell,
        destinationCell = null,
        previousDestination,
        nextDestination;
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
        if (originCell !== destinationCell) {

            // current absolute position ?
            var originalPos = originCell.offset();
            var origTop = originalPos.top;
            var origLeft = originalPos.left;

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
                },
                100, /* 'fast' = 200 ms, 'slow' = 600ms */
                function () {
                    // Animation complete : remettre le robot dans la cellule de destination
                    $(this).css({left:'0px', top:'0px'})
                        .appendTo(destinationCell.children().first()).offset(0, 0);
                }
            );
            moves++;
            $("#currentScore").text(moves + "");
            if (hasReachedObjective($robot, destinationCell)) {
                // submit the score !
                $.ajax({
                    url:document.URL + '/score',
                    type:'POST',
                    data:{score:moves},
                    async:false,
                    success:function (data) {
                        //alert("submitted");
                        $("#winModal").modal('show').find("a#retry").focus(); //so that enter does what we want
                    },
                    error:function () {
                        alert("not submitted");
                    },
                    statusCode:{
                        410:function () {
                            alert("to late ! - the game is finished");
                        }
                    }
                });
            }
        }
    }
}

function hasRobot(td) {
    return $(td).find(".robot").length > 0;
}

function hasReachedObjective(robot, td) {
    var robotClass = $(robot).attr("class");
    var robotColor = robotClass.substr(6); //remove "robot "
    robotColor = robotColor.substr(0, robotColor.indexOf(" ")); // remove " selected"
    return $(td).find("#objective").length > 0 && $("#objective").hasClass(robotColor);
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
                var col = td.parents("tr").children().index(td);
                nextCell = td.parents("tr").next().children()[col];
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
    $("#key-up").click(function () {
        moveRobot(DIRECTION_UP);
    });
    $("#key-down").click(function () {
        moveRobot(DIRECTION_DOWN);
    });
    $("#key-left").click(function () {
        moveRobot(DIRECTION_LEFT);
    });
    $("#key-right").click(function () {
        moveRobot(DIRECTION_RIGHT);
    });

    // robot from header selects the robot on the board
    $("a#headerRobot").click(function (e) {
        $(".selected").removeClass("selected");
        $("#robotForObjective").addClass("selected");
        e.preventDefault();
    });

    $('#headerGoal span').popover({placement:'bottom', title:"Bring the robot here !"});

    doPollScore();
    doPollTimer();
}

function doPollScore() {
    var $scores = $('#scores');
    var scoreUrl = document.URL + '/scores'; // scores for current game in current room
    $scores.load(scoreUrl, function (response, status, xhr) {
        if (status == "error") {
            alert("Error while retrieving the scores : status = " + status);
        }
        else {
            setTimeout(doPollScore, 5000);
        }

    });
}

function doPollTimer() {
    $.ajax({
            url:document.URL + '/status',
            success:function (data) {
                $('#progressBar').css('width', data.game.percentageDone + '%');
                if (data.game.percentageDone <= 0) {
                    $("#endOfGameModal").modal('show');
                }
                else {
                    setTimeout(doPollTimer, 1000);
                }
            },
            statusCode:{
                410:function () {
                    alert("The game you asked is finished .... ");
                    $("#endOfGameModal").modal('show').find("a#joinNextGame").focus();
                }
            }
        }
    )
    ;
}