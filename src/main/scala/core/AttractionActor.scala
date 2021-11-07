package core

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.config.ConfigFactory
import core.messages.{AttractionVisited, BuildAttractionPath, GetShortPath, VisitAttraction}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.util.Random



object AttractionActor{
  def props(id:Int, x:Int, y:Int, grid:ActorRef): Props = Props(new AttractionActor(id, x, y, grid))
}

class AttractionActor(val id:Int, val x:Int, val y:Int, val grid:ActorRef) extends Actor{

     val logger = LoggerFactory.getLogger("core.AttractionPoint")
     val config = ConfigFactory.load()
     val random = new Random(self.hashCode())
     val NOT_ATTRACTION_ID = -1

     def receive = {

       case message: VisitAttraction=>{
           val visitedBefore = message.ueLog.visitedBefore
           if(visitedBefore != null && visitedBefore.attractionId != -1){
                //ask grid to calculate intermediate cells
                grid ! GetShortPath(visitedBefore.x,visitedBefore.y,x,y, id, message.ueLog)
           }else {
                //adding first current attraction
                val firstAttractionTime = message.ueLog.ueTimeInfo.getFirstAttractionTime
                message.ueLog.addLocation(id, x, y, firstAttractionTime)
                message.ueLog.ueActor ! AttractionVisited(message.ueLog)
           }
       }

       case message: BuildAttractionPath=>{
          //adding cells between attractions
          val locationTimes:mutable.Buffer[Long] =  message.ueLog.ueTimeInfo.getLocationTimes(message.pathElements)
          var i = 0
          if(message.pathElements.size>0){
              for(cellCoordinates<-message.pathElements){
                  message.ueLog.addLocation(NOT_ATTRACTION_ID, cellCoordinates._1, cellCoordinates._2, locationTimes(i))
                  i+=1
              }
          }
          //adding current attraction(not the first)
          val nextAttractionTime = message.ueLog.ueTimeInfo.getNextAttractionTime
          message.ueLog.addLocation(message.nextId, message.nextCoordinates._1, message.nextCoordinates._2, nextAttractionTime)
          message.ueLog.ueActor ! AttractionVisited(message.ueLog)
       }

     }
}






