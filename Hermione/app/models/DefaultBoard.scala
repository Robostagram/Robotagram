package models


object DefaultBoard extends Board(16, 16){

  // murs sur la droite et la gauche
    for ( i <- 0 until height){
      setLeft(0, i)
      //setRight(width -1, i)
    }

  // murs en haut et en bas
  for ( j <- 0 until width){
     //setTop(0,j);
     //setBottom(height-1, j)
  }

  // mapping (coords) -> goal ... pour remplir la board après coup
  var goals = Map((5,2)->new Goal(Color.Red, Symbol.Star))
}
