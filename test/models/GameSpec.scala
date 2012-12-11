package models

import org.specs2.mutable._
import org.specs2.specification.Scope
import java.util.Date
import models.Color._
import models.Phase._
import models.Direction._

trait SimpleGame extends Scope {
  val board = Board.boardFromFile("app/resources/EmptyWithBorder.board", 0, "borderdBoard")
  // size is 17
  val goalR1 = new Goal(Color.Red, Symbol.ONE)
  val goalG2 = new Goal(Color.Green, Symbol.TWO)
  val goalB3 = new Goal(Color.Blue, Symbol.THREE)
  val goalY4 = new Goal(Color.Yellow, Symbol.FOUR)
  board.setGoal(1, 16, goalR1)
  board.setGoal(2, 16, goalG2)
  board.setGoal(3, 16, goalB3)
  board.setGoal(4, 16, goalY4)
  val robotRed = new Robot(Red, 1, 1)
  val robotGreen = new Robot(Green, 2, 2)
  val robotBlue = new Robot(Blue, 3, 3)
  val robotYellow = new Robot(Yellow, 4, 4)
  def ggame(goal: Goal) = new Game("simpleGame",
                      0,
                      board,
                      goal,
                      new Date(System.currentTimeMillis()),
                      new Date(System.currentTimeMillis() + 10*1000),
                      Map(Red -> robotRed,
                          Green -> robotGreen,
                          Blue -> robotBlue,
                          Yellow -> robotYellow),
                      GAME_1)
}

class GameSpec extends Specification {
  
  // getRobot
  
  "getRobot" should {
    "find robots where they are" in new SimpleGame {
      val game = ggame(goalR1)
      game.getRobot(0,0) must be_==(null)
      game.getRobot(1,0) must be_==(null)
      game.getRobot(0,1) must be_==(null)
      game.getRobot(1,2) must be_==(null)
      game.getRobot(2,1) must be_==(null)
      game.getRobot(1,1) must be_==(robotRed)
      game.getRobot(2,2) must be_==(robotGreen)
      game.getRobot(3,3) must be_==(robotBlue)
      game.getRobot(4,4) must be_==(robotYellow)
    }
  }
  
  // validate
  
  "validate" should {
    val solutionR = List(new Movement(Red, 1, 1, Down))
    val solutionG = List(new Movement(Green, 2, 2, Down))
    val solutionB = List(new Movement(Blue, 3, 3, Down))
    val solutionY = List(new Movement(Yellow, 4, 4, Down))
    "accept simple right solutions" in new SimpleGame {
      ggame(goalR1).validate(solutionR) must beTrue
      ggame(goalG2).validate(solutionG) must beTrue
      ggame(goalB3).validate(solutionB) must beTrue
      ggame(goalY4).validate(solutionY) must beTrue
    }
    "reject simple wrong solutions" in new SimpleGame {
      ggame(goalR1).validate(solutionG) must beFalse
      ggame(goalG2).validate(solutionB) must beFalse
      ggame(goalB3).validate(solutionY) must beFalse
      ggame(goalY4).validate(solutionR) must beFalse
    }
    val solutionR2 = List(new Movement(Green, 2, 2, Down),
        new Movement(Green, 2, 16, Left),
        new Movement(Red, 1, 1, Right),
        new Movement(Red, 16, 1, Down),
        new Movement(Red, 16, 16, Left))
    val solutionG2 = List(new Movement(Yellow, 4, 4, Down),
        new Movement(Yellow, 4, 16, Left),
        new Movement(Blue, 3, 3, Down),
        new Movement(Blue, 3, 16, Left),
        new Movement(Green, 2, 2, Right),
        new Movement(Green, 16, 2, Down),
        new Movement(Green, 16, 16, Left))
    "accept elaborated solutions" in new SimpleGame {
      ggame(goalR1).validate(solutionR2) must beTrue
      ggame(goalG2).validate(solutionR2) must beFalse
      ggame(goalG2).validate(solutionG2) must beTrue
      ggame(goalR1).validate(solutionG2) must beFalse
    }
  }

}