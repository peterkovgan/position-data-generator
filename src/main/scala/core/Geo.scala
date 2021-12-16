package core


import com.typesafe.config.ConfigFactory

import scala.collection.mutable._

/**
 * This class is to work with Seattle Map (see USA-Seattle_cells.csv)
 * It is an optional functionality (and not final, but works)
 *
 */
object Geo {
  val config = ConfigFactory.load
  val ob_wide = config.getInt("akka.geo.ob_wide") //must be odd - will define what is obstacle (the width/height without station that will be considered an obstacle)
  val ob_diameter = config.getInt("akka.geo.ob_diameter") //must be odd - will define what is obstacle radius
  val COMMA_DELIMITER = ","
  val mapAllAttractions = Map[Int, ArraySeq[String]]()
  val mapAllAttractionsByCoordinates = Map[(Int, Int), Array[String]]()
  val mapProvenAttractionsByCoordinates = Map[(Int, Int), Array[String]]()
  val obstaclesBuffer = new java.util.ArrayList[String]()
  val width = config.getInt("akka.geo.width")
  val height = config.getInt("akka.geo.height")
  var fileName = config.getString("akka.geo.fileName")
  val cells = Array.ofDim[Int](width, height)
  val obstacles = Array.ofDim[Int](width, height)
  for (x <- 0 until width) {
    for (y <- 0 until height) {
      cells(x)(y) = 0
    }
  }


  def doAll: GeoSummary = {
    readCsv
    generateMapObstacles
    writeObstacles
    selectProvenAccessibleGNodeBs
    val javaListProven = new java.util.ArrayList[String]()
    val provenLocations = mapProvenAttractionsByCoordinates.keys.toList.map(e => javaListProven.add(e._1 + "," + e._2))
    GeoSummary(mapProvenAttractionsByCoordinates, javaListProven, obstaclesBuffer)
  }


  private def writeObstacles = {
    for (x <- 0 until width) {
      for (y <- 0 until height) {
        if (obstacles(x)(y) == 1) {
          obstaclesBuffer.add(x + "," + y)
        }
      }
    }
  }

  private def selectProvenAccessibleGNodeBs = { //not exactly, some may be isolated

    for (x <- 0 until width) {
      for (y <- 0 until height) {
        if (obstacles(x)(y) != 1 && cells(x)(y) == 1) {
          val value = mapAllAttractionsByCoordinates.get((x, y))
          if (value.isDefined) {
            mapProvenAttractionsByCoordinates.put((x, y), value.get)
          }
        }
      }
    }
    println("Proven=" + mapProvenAttractionsByCoordinates.size)

  }


  def readCsv = {
    val bufferedSource = io.Source.fromFile(fileName)
    var i = 0
    for (line <- bufferedSource.getLines) {
      val cols = line.split(",").map(_.trim)
      val key = cols.head
      val values = cols.tail
      if (i != 0 && !key.equals("")) {
        val x = values(0).toInt
        val y = values(1).toInt
        mapAllAttractions.put(key.toInt, values)
        mapAllAttractionsByCoordinates.put((x, y), cols)
        cells(x)(y) = 1
      }
      i += 1
    }
    bufferedSource.close
    println("Map " + mapAllAttractions.size)
    println("Preliminary " + mapAllAttractionsByCoordinates.size)
  }

  //brut forth connecting close cells
  def generateMapObstacles: Unit = {

    for (x <- 1 until width - 1) {
      for (y <- 1 until height - 1) {
        if (!busyRectangleAround(x, y)) {
          fillObstacles(x, y)
        }
      }
    }
  }

  def busyRectangleAround(x: Int, y: Int): Boolean = {

    if (cells(x)(y) == 1) return true

    val step = ob_wide / 2
    for (h <- (x - step) to (x + step)) {
      for (v <- (y - step) to (y + step)) {
        if (h >= 0 && h < width && v >= 0 && v < height) {
          if (cells(h)(v) == 1) return true
        }
      }
    }
    false
  }

  def fillObstacles(x: Int, y: Int) = {
    val step = ob_diameter / 2
    for (h <- (x - step) to (x + step)) {
      for (v <- (y - step) to (y + step)) {
        if (h >= 0 && h < width && v >= 0 && v < height) {
          obstacles(h)(v) = 1
        }
      }
    }
  }


}
