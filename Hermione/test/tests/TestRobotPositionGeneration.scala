package tests

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import models._
import collection.immutable.HashSet

class TestRobotPositionGeneration extends Specification{

  "generate random position for list of robots" in {
    val robots:Array[Robot] =   controllers.Application.generateRobots();
    var setPos:Set[Tuple2[Int,Int]] = new HashSet[Tuple2[Int,Int]];
    for(robot <- robots){
       if(robot.posX==7){ robot.posY must_!= 7}
       if(robot.posX==7){ robot.posY must_!= 8}
       if(robot.posX==8){ robot.posY must_!= 7}
       if(robot.posX==8){ robot.posY must_!= 8}
      
      setPos.contains((robot.posX,robot.posY)) must beFalse
      setPos+((robot.posX,robot.posY))
    }
  }

}
