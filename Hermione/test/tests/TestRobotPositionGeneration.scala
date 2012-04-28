package tests

import org.specs2.mutable.Specification
import models._
import collection.immutable.HashSet

object gameMockMiddlePosition extends Game(DefaultBoard,Goal.randomGoal(), 120){
  var call:Int = 0
  val returns = Array(7,7,8,8,7,8,8,7,1,2,3,4,5,6,9,10,11,12,13,14,15);
  override def getRandomPosition(max:Int)={
    var result = 12
    if(call<returns.size){
      result = returns(call)
    }
    call+=1
    result
  }
}
object gameMockRobotOverlap extends Game(DefaultBoard,Goal.randomGoal(), 120){
  var call:Int = 0
  val returns = Array(12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,1,2,3,4,5,6,9,10,11,12);
  override def getRandomPosition(max:Int)={
    var result = 13
    if(call<returns.size){
      result = returns(call)
    }
    call+=1
    result
  }
}

class TestRobotPositionGeneration extends Specification{

   "generate random robots list  produce non center postion" in {
      val robots:List[Robot] =   gameMockMiddlePosition.robots;
      for(robot <- robots){
         if(robot.posX==7){ robot.posY must_!= 7}
         if(robot.posX==7){ robot.posY must_!= 8}
         if(robot.posX==8){ robot.posY must_!= 7}
         if(robot.posX==8){ robot.posY must_!= 8}
      }
    }

    "produce unique position for each robot" in {
      var setPos:Set[Tuple2[Int,Int]] = new HashSet[Tuple2[Int,Int]];
      val robots:List[Robot] =   gameMockRobotOverlap.robots
      for(robot <- robots){
        setPos.contains((robot.posX,robot.posY)) must beFalse
        setPos+=((robot.posX,robot.posY))
      }
    }

}
