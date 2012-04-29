package models

class Player(val name:String) {
   var highScore:Int = 0;

  def scored(newScore:Int) {
    highScore = if (highScore < newScore) newScore else highScore
  };
}
