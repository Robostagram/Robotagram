package controllers

object MessageID extends Enumeration {
  type MessageID = Value
  val USER_REFRESH = Value("USER_REFRESH")
  val SOLUTION_FOUND = Value("SOLUTION_FOUND")
  val TIME_UP = Value("TIME_UP")
  val NEW_ROUND = Value("NEW_ROUND")
}