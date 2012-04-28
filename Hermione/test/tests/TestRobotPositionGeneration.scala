package tests

import org.specs2.mutable.Specification
import models._
import collection.immutable.HashSet
import org.specs2.mock.Mockito
import play.api.mvc.Controller

object gameMock extends Game(DefaultBoard,Goal.randomGoal(), 120){
  var call:Int = 0
  val returns = Array(7,7,8,8,7,8,8,7);
  override def getRandomPosition(max:Int)={
    var result = 12
    if(call<returns.size){
      result = returns(call)
    }
    call+=1
    result
  }
}

class TestRobotPositionGeneration extends Specification with Mockito{

   "generate random robots list  produce non center postion" in {
      val game:Game = gameMock
      val robots:List[Robot] =   game.robots;
      for(robot <- robots){
         if(robot.posX==7){ robot.posY must_!= 7}
         if(robot.posX==7){ robot.posY must_!= 8}
         if(robot.posX==8){ robot.posY must_!= 7}
         if(robot.posX==8){ robot.posY must_!= 8}
      }
    }

    /*"produce unique position for each robot" in {
     /* var setPos:Set[Tuple2[Int,Int]] = new HashSet[Tuple2[Int,Int]];
      val appli:Controller =  mock[controllers.Application]
      appli.getRandomPosition(anyInt) returns 12 thenReturns 12 thenReturns 12 thenReturns 12
      val robots:List[Robot] =   appli.generateRobots(DefaultBoard);
      for(robot <- robots){
        setPos.contains((robot.posX,robot.posY)) must beFalse
        setPos+=((robot.posX,robot.posY))
      }*/
    }    */

}
