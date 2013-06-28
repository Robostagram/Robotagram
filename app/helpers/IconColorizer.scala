package helpers

import java.io.File
import javax.imageio.ImageIO
import java.awt.Color
import models.Color._

object IconColorizer {
  
  def toFile(filename: String) = new File("public/images/symbols/" + filename)
  def toImage(filename: String) = ImageIO.read(toFile(filename))
  
  val iconMap = Map(
    "moon" -> "glyphicons_230_moon.png",
    "drink" -> "glyphicons_273_drink.png",
    "sun" -> "glyphicons_231_sun.png",
    "gear" -> "glyphicons_019_cogwheel.png",
    "star" -> "glyphicons_049_star.png",
    "planet" -> "glyphicons_340_globe.png",
    "skull" -> "glyphicons_290_skull.png",
    "bowling" -> "glyphicons_315_bowling.png",
    "electricity" -> "glyphicons_205_electricity.png",
    "cloud" -> "glyphicons_232_cloud.png",
    "water" -> "glyphicons_092_tint.png",
    "ice" -> "glyphicons_021_snowflake.png",
    "fire" -> "glyphicons_022_fire.png",
    "robot" -> "robot.png"
  ).map(pair => pair._1 -> toImage(pair._2))
  val colors = Map(Red -> (230,30,30), Green -> (30,210,30), Blue -> (30,30,230), Yellow -> (230,150,30))
  
  def generate = {
    for {
      imagePair <- iconMap
      colorPair <- colors
    } {
      val img = imagePair._2
      val (r,g,b) = colorPair._2
      val out = toFile(colorPair._1.toString.toLowerCase + "_" + imagePair._1 + ".png")
      val g2d = img.createGraphics
      for {
        x <- 0 until img.getWidth
        y <- 0 until img.getHeight
      } {
        val alpha = (img.getRGB(x, y) & 0xff000000) >>> 24
        g2d.setColor(new Color(r,g,b,alpha))
        g2d.fillRect(x,y,1,1)
      }
      g2d.dispose
      ImageIO.write(img, "png", out)
    }
  }
  
  def main(args: Array[String]) = generate

}