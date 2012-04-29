(function () {
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

    function selectNextRobot() {
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
        curColorIndex += 1;
        if (curColorIndex >= ROBOT_COLORS.length) {
            curColorIndex = 0;
        }

        selectByColor(ROBOT_COLORS[curColorIndex]);
    }

    function selectPreviousRobot() {
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
        curColorIndex -= 1;
        if (curColorIndex < 0) {
            curColorIndex = ROBOT_COLORS.length - 1;
        }

        selectByColor(ROBOT_COLORS[curColorIndex]);
    }

    function selectByColor(colorName) {
        $(".selected").toggleClass("selected");
        $(".robot." + colorName).toggleClass("selected");
    }


    function robotClickHandler() {
        var $this = $(this);
        if (!$this.is(".selected")) {
            $(".selected").toggleClass("selected");
        }

        $(this).toggleClass("selected");
    }

    function retryClick(event) {
        $('#container').load('/newGame/' + encodeURI($("#nicknameDisplay").text() + "/" + moves), function () {
            //reattach event because we load our listeners on previous dom objects
            initListeners();
            $("#moves").val(0);
            moves = 0;
        });
    }

    function loadNewGame() {
        var container = $('#container');
        var user = $('#nickname').val();
        container.load('/newGame/' + encodeURI(user) + "/0", function () {
            initListeners();
        });
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
                $("#moves").text(moves + " Moves");
                if (hasReachedObjective($robot, destinationCell)) {
                    $("#winModal").modal('show');
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
        $(".robot").click(robotClickHandler);
        $("#retry").click(retryClick);

        //on triche, et les touches affichées marchent comme un clavier
        $("#key-up").click(function () {
            moveRobot(DIRECTION_UP)
        });
        $("#key-down").click(function () {
            moveRobot(DIRECTION_DOWN)
        });
        $("#key-left").click(function () {
            moveRobot(DIRECTION_LEFT)
        });
        $("#key-right").click(function () {
            moveRobot(DIRECTION_RIGHT)
        });
        doPollScore();
        doPollTimer();
    }

    function doPollScore() {
        var scores = $('#scores');
        scores.load('/scores', function () {
            setTimeout(doPollScore, 5000);
        });
    }

    function doPollTimer() {
        $.ajax({
                url:'/progress',
                success:function(data){
                    $('#progressBar').css('width',data+'%');
                    setTimeout(doPollTimer, 1000);
                }
            }
        );
    }

    $(document).ready(function () {
        $("#nickModal").modal('show');
        $("#play").click(loadNewGame);
        $("#nickname").keypress(function (e) {
            if (e.keyCode == 13) {
                $("#nickModal").modal('hide');
                loadNewGame();
            }
        });
    })


})();
