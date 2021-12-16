package core


/**
 * Every step in the UE path is a LogElement
 */
case class LogElement(attractionId:Int, x:Int, y:Int, ueId:Int, cellId:Int, locationTime:Long)
