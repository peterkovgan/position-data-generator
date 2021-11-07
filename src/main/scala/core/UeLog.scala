package core

import akka.actor.ActorRef
import algos.CellUtil
import scala.collection.mutable.Buffer

case class UeLog(ueActor: ActorRef, grid:ActorRef, ueId:Int, ueTimeInfo: UeTimeInfo) {


  var step = 0;

  val allVisited:Buffer[LogElement] = Buffer.empty

  def addLocation(attractionId: Int, x:Int, y:Int, locationTime:Long): Unit = {
      if(attractionId != -1) {
        step += 1
      }

      allVisited.addOne(LogElement(attractionId, x, y, ueId, CellUtil.cellId(x,y), locationTime))
  }

  def visitedBefore={
      if(allVisited.size > 0){
          allVisited.last
      }else{
          null
      }
  }

  def getStep(): Int ={
    step
  }
}
