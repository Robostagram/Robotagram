(function(){
    function keypressHandler(event){
        if(DIRECTION_UP<=event.which && event.which<=DIRECTION_RIGHT && $(".selected").hasClass('robot') ){
            moveRobot(event.which);
        }
    }

    function robotClickHandler(event){
        $(".selected").removeClass("selected");
        $(this).addClass("selected");
    }

    function retryClick(event){
        $("#boardZone").load("/current/reload", function(){
            //reattach event because we load our listeners on previous dom objects
            $(".robot").click(robotClickHandler);
        });
    }

    function loadNewGame(){
        var container = $('#container');
        var user = $('#nickname').val();
        $('#nicknameDisplay').text(user);
        container.load('/newGame/'+encodeURI(user), function(){
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

    var moves = 0;

    function moveRobot(direction){
        var robot = null;
        var parentCell = null
        var destinationCell = null;
        do{
            robot = $(".selected");
            parentCell = robot.parent().parent()
            destinationCell = nextCell(parentCell, direction);
            robot.detach();
            robot.appendTo($(destinationCell).children()[0])
        } while(destinationCell !== parentCell)
        moves++;
        $("#moves").text(moves+" Moves");
        if(hasReachedObjective(robot, parentCell)){
            $("#winModal").modal('show');
        }
    }

    function hasRobot(td) {
        return $(td).children().children().filter(".robot").length > 0;
    }

    function hasReachedObjective(robot, td) {
        var robotClass = $(robot).attr("class");
        var robotColor = robotClass.substr(6); //remove "robot "
        robotColor = robotColor.substr(0, robotColor.indexOf(" ")); // remove " selected"
        return $(td).children().children().filter("#objective").length > 0 && $("#objective").hasClass(robotColor);
    }

    function nextCell(td, direction) {
        var nextCell = null;
        switch(direction){
        case DIRECTION_UP:
            if(!td.is(".wall-top")){
                nextCell = td.parent().prev().children()[td.parent().children().index(td)];
            }
            break;
        case DIRECTION_DOWN:
            if(!td.is(".wall-bottom")){
                nextCell = td.parent().next().children()[td.parent().children().index(td)];
            }
            break;
        case DIRECTION_LEFT:
            if(!td.is(".wall-left")){
                nextCell = td.prev();
            }
            break;
        case DIRECTION_RIGHT:
            if(!td.is(".wall-right")){
                nextCell = td.next();
            }
            break;
        }
        if(nextCell != null && !hasRobot(nextCell)) {

            return nextCell
        } else {
            return td;
        }
    }
      function initListeners(){
          $(window).keypress(keypressHandler);
          $(".robot").click(robotClickHandler);
          $("#retry").click(retryClick);

          //on triche, et les touches affichées marchent comme un clavier
          $("#key-up").click(function(){
              moveRobot(DIRECTION_UP)
          });
          $("#key-down").click(function(){
              moveRobot(DIRECTION_DOWN)
          });
          $("#key-left").click(function(){
              moveRobot(DIRECTION_LEFT)
          });
          $("#key-right").click(function(){
              moveRobot(DIRECTION_RIGHT)
          });
      }
    $(document).ready(function(){
        $("#nickModal").modal('show');
        $("#play").click(loadNewGame);
        $("#nickname").keypress(function(e){
            if(e.keyCode == 13) {
                $("#nickModal").modal('hide');
                loadNewGame();
            }
        })


    })


})();
