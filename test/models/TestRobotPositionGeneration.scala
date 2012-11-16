package models

import org.specs2.mutable.Specification
import models._
import models.Color._
import collection.immutable.HashSet
import collection.immutable.HashMap
import java.util.Date

object MockGame extends Game("mockmiddle", DefaultBoard, Goal.randomGoal, new Date, new Date, new HashMap[Color, Robot]){}

class TestRobotPositionGeneration extends Specification{

   "generate random robots list  produce non center postion" in {
      for(robot <- MockGame.robots.values){
         if(robot.posX==7){ robot.posY must_!= 7}
         if(robot.posX==7){ robot.posY must_!= 8}
         if(robot.posX==8){ robot.posY must_!= 7}
         if(robot.posX==8){ robot.posY must_!= 8}
      }
    }

    "produce unique position for each robot" in {
      var setPos:Set[Tuple2[Int,Int]] = new HashSet[Tuple2[Int,Int]];
      for(robot <- MockGame.robots.values){
        setPos.contains((robot.posX,robot.posY)) must beFalse
        setPos+=((robot.posX,robot.posY))
      }
    }

}
