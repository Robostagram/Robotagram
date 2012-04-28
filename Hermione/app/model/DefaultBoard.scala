package model


object DefaultBoard extends Board(16, 16){

  // murs sur la gauche
    for ( i <- 0 to width){
      cells(0)(i) = cells(0)(i).withEastWall()
    }
  // murs en haut
  for ( j <- 0 to height){
    cells(j)(0) = cells(j)(0).withNorthWall()
  }

  // mapping (coords) -> goal ... pour remplir la board après coup
  var goals = Map((5,2)->new Goal(Color.Red, Symbol.Star))




}
