package models


object DefaultBoard extends Board(16, 16){

  // murs sur la gauche
    for ( i <- 0 until width){
      cells(i)(0) = cells(i)(0).withEastWall()
    }
  // murs en haut
  for ( j <- 0 until height){
    cells(0)(j) = cells(0)(j).withNorthWall()
  }

  // mapping (coords) -> goal ... pour remplir la board après coup
  var goals = Map((5,2)->new Goal(Color.Red, Symbol.Star))
}
