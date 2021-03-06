//=======================
// module robotagram.ui
// ======================
//
// contains stuff about the user interface

// creating root name space robotagram if not done already ...
window["robotagram"] = window["robotagram"] || {} ; //initialize robotagram root name space if not done

// creating name space robotagram.ui
// (using the patterns described in : http://www.codethinked.com/preparing-yourself-for-modern-javascript-development )
window["robotagram"]["ui"] = (function($, undefined){


// resize the contents of the top bar so that it always fit the window
function resizeTopBarContents(){
    /*
    * header#container looks like this :
    *    ... | a#brand | ... | div#headerExtraContent | ... | ul#userInfoBar | ....
    *
    * the zone in the middle (div#headerExtraContent) must always take as much space as possible
    *
    * With js, we compute the actual width of the container and all the elements after and before
    *  the headerExtraContent zone. We then make it as wide as possible.
    * */
    var $headerContainer = $("header").find(".container").first();
    var containerFullWidth = $headerContainer.width();// the actual width as displayed on the screen


    var $centerZone = $headerContainer.find("#headerExtraContent"),
            totalWidthOnTheLeft = 0,
            totalWidthOnTheRight = 0;
    // compute the sum of the width of all the elements that come before and after (outerwidth + margins)
    $centerZone.prevAll().each(function(){
                totalWidthOnTheLeft = totalWidthOnTheLeft + $(this).outerWidth() + parseInt($(this).css("marginLeft"));
            });
    $centerZone.nextAll().each(function(){
            totalWidthOnTheRight = totalWidthOnTheRight + $(this).outerWidth() + parseInt($(this).css("marginLeft"));
        });

    // widthInTheMiddle is the difference of the total container width and the width of other elements of the header
    $centerZone.width(containerFullWidth - totalWidthOnTheLeft - totalWidthOnTheRight - 4 /*ugly !!*/);


    // sometimes we have more contents in the centered Zone that need to take as much space as possible too
    // div#headerExtraContent contains : ul#headerInfo
    $scoreZone = $centerZone.find("#headerInfo");
    // only if it is part of the page (currently only in the game page - zone for the current game information)
    if($scoreZone.length> 0){
        /*
         * the game Info zone (#headerInfo) looks something like this (with li wrapping all the links):
         *
         * .divider-vertical | a#headerRobot | a#headerArrow | a#headerGoal | a#currentScore | .divider-vertical | a#timeLeft | li#timeProgress
         *
         * all but li#timeProgress have a fixed width, and we want li#timeProgress to take as much space as possible in the containing zone
         */

        $scoreZone = $scoreZone.first();
        containerFullWidth = $scoreZone.width();
        totalWidthOnTheLeft = 0;
        totalWidthOnTheRight = 0;

        $timer = $scoreZone.find("#timeProgress");

        $timer.prevAll().each(function(){
            totalWidthOnTheLeft = totalWidthOnTheLeft + $(this).outerWidth() + parseInt($(this).css("marginLeft"), 10) + parseInt($(this).css("marginRight"), 10);
        });

        $timer.nextAll().each(function(){
            totalWidthOnTheRight = totalWidthOnTheRight + $(this).outerWidth() + parseInt($(this).css("marginLeft"), 10) + parseInt($(this).css("marginRight"), 10);
        });

        // fix the width of the progress bar
        $timer.width(containerFullWidth - totalWidthOnTheLeft - totalWidthOnTheRight - 1 /*ugly :-/*/);
    }
}


// shitty loading indicator to show something is happening ... putting it on body so far
function showLoading(){
    $("body").addClass("loading");
}
function hideLoading(){
    $("body").removeClass("loading");
}

// set up the tooltip on current robot and target objective ... should be less intrusive
function highlightRobotAndObjective(){
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


  // Exports
  // ==========================

  // makes public members public
  return {
    "resizeTopBarContents": resizeTopBarContents,
    "showLoading" : showLoading,
    "hideLoading" : hideLoading,
    "highlightRobotAndObjective" : highlightRobotAndObjective
  }
})(jQuery);
