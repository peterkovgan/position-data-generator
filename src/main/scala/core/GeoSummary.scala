package core


import scala.collection.mutable.Map

case class GeoSummary(mapProvenAttractionsByCoordinates: Map[(Int,Int), Array[String]], provenLocations:java.util.List[String], obstaclesBuffer:java.util.List[String])
