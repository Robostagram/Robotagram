package models


object DefaultBoard extends Board(0, "DefaultBoard", 16, 16) {

  // murs sur la droite et la gauche
  for (i <- 0 until height) {
    setLeft(0, i)
    setRight(width - 1, i)
  }

  // murs en haut et en bas
  for (j <- 0 until width) {
    setTop(j, 0);
    setBottom(j, height - 1)
  }

  // mapping (coords) -> goal ... pour remplir la board après coup
  var goalsToAdd = Map(
    (1, 5) -> new Goal(Color.Blue, Symbol.FOUR),
    (1, 11) -> new Goal(Color.Red, Symbol.FOUR),
    //TODO : (2, 7) ->   Tourbillon de la mort ....
    (3, 14) -> new Goal(Color.Green, Symbol.THREE),
    (4, 3) -> new Goal(Color.Red, Symbol.ONE),
    (4, 9) -> new Goal(Color.Blue, Symbol.TWO),
    (5, 6) -> new Goal(Color.Green, Symbol.TWO),
    (6, 1) -> new Goal(Color.Yellow, Symbol.THREE),
    (6, 12) -> new Goal(Color.Yellow, Symbol.ONE),
    (9, 12) -> new Goal(Color.Blue, Symbol.ONE),
    (10, 3) -> new Goal(Color.Blue, Symbol.THREE),
    (10, 10) -> new Goal(Color.Yellow, Symbol.TWO),
    (11, 5) -> new Goal(Color.Green, Symbol.ONE),
    (12, 2) -> new Goal(Color.Yellow, Symbol.FOUR),
    (12, 14) -> new Goal(Color.Red, Symbol.THREE),
    (13, 4) -> new Goal(Color.Red, Symbol.TWO),
    (14, 11) -> new Goal(Color.Green, Symbol.FOUR)
  )

  for (((x, y), v) <- goalsToAdd) {
    setGoal(y, x, v)
  }

  var rightWalls = Array(
    (0, 2), (0, 8),
    (1, 4), (1, 10),
    (2, 7),
    (3, 14),
    (4, 3), (4, 9),
    (5, 5),
    (6, 1), (6, 11),
    (7, 6), (7, 8),
    (8, 6), (8, 8),
    (9, 11),
    (10, 3), (10, 10),
    (11, 5),
    (12, 1), (12, 14),
    (13, 3),
    (14, 10),
    (15, 3), (15, 13)
  )

  for ((x, y) <- rightWalls) {
    setRight(y, x)
  }

  var bottomWalls = Array(
    (0, 11),
    (1, 5), (1, 15),
    (2, 7), (2, 14),
    (3, 0),
    (4, 3), (4, 6), (4, 9),
    (5, 1),
    (6, 7), (6, 8), (6, 12),
    (8, 7), (8, 8), (8, 12),
    (9, 0), (9, 15),
    (10, 3), (10, 5), (10, 10),
    (11, 14),
    (12, 2), (12, 4),
    (14, 11)
  )

  for ((x, y) <- bottomWalls) {
    setBottom(y, x)
  }
}
