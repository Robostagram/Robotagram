package models

import models.Color._
import models.Direction._

case class Movement(val color: Color, val x: Int, val y: Int, val direction: Direction)