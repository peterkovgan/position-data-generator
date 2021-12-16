package algos

import com.typesafe.config.ConfigFactory


/**
 * This class able to create unique cell ID from X and Y
 */
object CellUtil {
  val config = ConfigFactory.load()
  val width  = config.getInt("akka.grid.width")
  def cellId(x: Int, y: Int):Int = {
      y * width + x
  }
}
