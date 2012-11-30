package models

sealed abstract class Phase(val intValue: Int)
case object GAME_1 extends Phase(1)
case object GAME_2 extends Phase(2)
case object SHOW_SOLUTION extends Phase(3)