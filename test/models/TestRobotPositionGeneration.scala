package models

import org.specs2.mutable.Specification
import models._
import models.Color._
import models.Phase._
import collection.immutable.HashSet
import collection.immutable.HashMap
import java.util.Date

object MockGame extends Game("mockmiddle", 0l, DefaultBoard, Goal.randomGoal, new Date, new Date, new HashMap[Color, Robot], GAME_1){}

class TestRobotPositionGeneration extends Specification{

   "generate random robots list  produce non center postion" in {
      for(robot <- MockGame.robots.values){
         if(robot.col==7){ robot.row must_!= 7}
         if(robot.col==7){ robot.row must_!= 8}
         if(robot.col==8){ robot.row must_!= 7}
         if(robot.col==8){ robot.row must_!= 8}
      }
    }

    "produce unique position for each robot" in {
      var setPos:Set[Tuple2[Int,Int]] = new HashSet[Tuple2[Int,Int]];
      for(robot <- MockGame.robots.values){
        setPos.contains((robot.col,robot.row)) must beFalse
        setPos+=((robot.col,robot.row))
      }
    }

}
