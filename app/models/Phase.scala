package models

object Phase extends Enumeration {
  type Phase = Value
  val GAME_1 = Value("GAME_1")
  val GAME_2 = Value("GAME_2")
  val SHOW_SOLUTION = Value("SHOW_SOLUTION")
}