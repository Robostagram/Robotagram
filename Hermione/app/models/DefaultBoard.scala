package models

object DefaultBoard extends Board(16, 16){

  // murs sur la droite
    for ( i <- 0 until width){
      withRight(i, 0, true)
    }
  // murs en haut
  for ( j <- 0 until height){
     withTop(0,j, true);
  }

  // mapping (coords) -> goal ... pour remplir la board après coup
  var goals = Map((5,2)->new Goal(Color.Red, Symbol.Star))
}
