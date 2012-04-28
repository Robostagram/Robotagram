package tests

import org.specs2.mutable.Specification
import models._
import collection.immutable.HashSet
import org.specs2.mock.Mockito
import play.api.mvc.Controller

class TestRobotPositionGeneration extends Specification with Mockito{

  "generate random position " should {
      "produce non center postion" in {
      var mock:Game = mock[Game]
        mock.getRandomPosition(anyInt) returns 7 thenReturns 7 thenReturns 7 thenReturns 8 thenReturns 8 thenReturns 7 thenReturns 8 thenReturns 8
      val robots:List[Robot] =   mock.generateRobots(DefaultBoard);
      for(robot <- robots){
         if(robot.posX==7){ robot.posY must_!= 7}
         if(robot.posX==7){ robot.posY must_!= 8}
         if(robot.posX==8){ robot.posY must_!= 7}
         if(robot.posX==8){ robot.posY must_!= 8}
      }
    }

    "produce unique position for each robot" in {
     /* var setPos:Set[Tuple2[Int,Int]] = new HashSet[Tuple2[Int,Int]];
      val appli:Controller =  mock[controllers.Application]
      appli.getRandomPosition(anyInt) returns 12 thenReturns 12 thenReturns 12 thenReturns 12
      val robots:List[Robot] =   appli.generateRobots(DefaultBoard);
      for(robot <- robots){
        setPos.contains((robot.posX,robot.posY)) must beFalse
        setPos+=((robot.posX,robot.posY))
      }*/
    }
  }

}
