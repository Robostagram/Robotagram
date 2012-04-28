package models


object DefaultBoard extends Board(16, 16){

  // murs sur la droite
    for ( i <- 0 until height){
      withRight(i, 0)
    }
  // murs en haut
  for ( j <- 0 until width){
     withTop(0,j);
  }

  // mapping (coords) -> goal ... pour remplir la board après coup
  var goals = Map((5,2)->new Goal(Color.Red, Symbol.Star))
}
