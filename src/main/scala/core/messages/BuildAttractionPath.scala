package core.messages

import core.UeLog

import scala.collection.mutable.Buffer

case class BuildAttractionPath(pathElements:Buffer[(Int, Int)], nextId:Int, nextCoordinates:(Int, Int), ueLog:UeLog)
