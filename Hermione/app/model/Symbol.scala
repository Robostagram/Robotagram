package model

abstract class Symbol {

  case class Star() extends Symbol

  case class Planet() extends Symbol

  case class Gear() extends Symbol

  case class Moon() extends Symbol

  case class Sun() extends Symbol

}
