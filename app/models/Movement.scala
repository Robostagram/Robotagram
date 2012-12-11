package models

import models.Color._
import models.Direction._

case class Movement(val color: Color, val col: Int, val row: Int, val direction: Direction)