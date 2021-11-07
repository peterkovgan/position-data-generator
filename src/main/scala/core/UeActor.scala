package core

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.config.ConfigFactory
import core.messages.{AttractionVisited, CreateUe, GetFirstAttraction, GetNextAttraction, RegisterDayTrip, UeFirstAttraction, UeIsReady, UeNextAttraction, UeStartMove, VisitAttraction}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.util.Random
import java.text.SimpleDateFormat
import java.util

import core.Main.geoSummary

object UeActor{
  def props(grid:ActorRef, registry:ActorRef, ueId:Int): Props = Props(new UeActor(grid, registry, ueId))
}

class UeActor(grid:ActorRef, registry:ActorRef, ueId:Int) extends Actor{

    val logger                                     =  LoggerFactory.getLogger("core.UeProfileActor")
    val config                                     =  ConfigFactory.load
    //space
    import scala.jdk.CollectionConverters
    val attractionLocations:java.util.List[java.lang.String] =  {
        if(geoSummary.isDefined){
          val list = new util.ArrayList[String]
          geoSummary.get.provenLocations.forEach(e=>list.add(e))
          list
        }else{
            config.getStringList("akka.attractions.location")
        }
    }
    val probabilityToAddTheLocationToUe            =  config.getInt("akka.attractions.probabilityToAddTheLocationToUe")
    val attractionsPath: mutable.Buffer[String]    =  mutable.Buffer.empty
    //time
    var thisDay = 0
    val start                                      =  config.getString("akka.time.start")
    val startTimestamp                             =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(start).getTime
    val dailyStartUeFluctuationMinutes             =  config.getInt("akka.time.dailyStartUeFluctuationMinutes")
    val roundTripHours                             =  config.getInt("akka.time.roundTripHours")
    val roundTripHoursMin                          =  config.getInt("akka.time.roundTripHoursMin")
    val cellTimeTypesCrossingCellSec               =  config.getIntList("akka.time.cellTimeTypesCrossingCellSec")
    val cellTimeTypesCrossingCellSecRandomPercent  =  config.getInt("akka.time.cellTimeTypesCrossingCellSecRandomPercent")
    val mixTransportBetweenDays                    =  config.getBoolean("akka.time.mixTransportBetweenDays")


    val minAttractions  =  config.getInt("akka.geo.minAttractions")
    val maxAttractions  =  config.getInt("akka.geo.maxAttractions")


    var ueTimeInfo:UeTimeInfo = null


    def createProfile: Unit = {
      var addedTargetLocations=0
      val random = new Random(self.hashCode())
      val homeIndex = random.nextInt(attractionLocations.size())
      val homeLocation =  attractionLocations.get(homeIndex)
      attractionsPath.addOne(homeLocation)
      attractionLocations.remove(homeIndex)
      val shuffled = Random.shuffle(attractionLocations.toArray.toSeq)

      while(addedTargetLocations==0) {
        if(Main.geoSummary.isDefined){
          var i = 0
          for (location <- shuffled) {
            val attrNumber = minAttractions + Random.nextInt(maxAttractions-minAttractions)
            if (i < attrNumber) {
              attractionsPath.addOne(location.toString)
              addedTargetLocations += 1
              i+=1
            }
          }
        } else{
          for (location <- shuffled) {
            if (random.nextInt(100) <= probabilityToAddTheLocationToUe) {
              attractionsPath.addOne(location.toString)
              addedTargetLocations += 1
            }
          }
        }

      }

      attractionsPath.addOne(homeLocation)
      ueTimeInfo = UeTimeInfo(startTimestamp, dailyStartUeFluctuationMinutes, roundTripHours, roundTripHoursMin, cellTimeTypesCrossingCellSec, cellTimeTypesCrossingCellSecRandomPercent,attractionsPath, mixTransportBetweenDays)
    }

  def receive = {
      case CreateUe => {
          createProfile
          sender ! UeIsReady(context.self)
      }
      case UeStartMove => {
          ueTimeInfo.moveOneDay(thisDay)
          thisDay+=1
          val home = attractionsPath(0)
          val x = home.split(",")(0).toInt
          val y = home.split(",")(1).toInt
          grid ! GetFirstAttraction(x,y)
      }
      case message:UeFirstAttraction=>{
          val attraction = message.cell.attraction
          if(message.cell.attraction.isDefined){
            val attractionActor = attraction.get
            val ueLog = UeLog(context.self, grid, ueId, ueTimeInfo)
            attractionActor ! VisitAttraction(ueLog)
          }else{
             logger.error("The object in the cell is not an attraction actor")
          }
      }
      case message:AttractionVisited =>{
           val ueLog  = message.ueLog
           if(attractionsPath.size > ueLog.getStep) {
             val target = attractionsPath(ueLog.getStep)
             val x = target.split(",")(0).toInt
             val y = target.split(",")(1).toInt
             grid ! GetNextAttraction((x, y),ueLog)
           }else{
             logger.info("The daily trip is ended")
             registry !  RegisterDayTrip(ueLog,context.self)
           }
      }
      case message: UeNextAttraction=>{
        val attraction = message.cell.attraction
        if(attraction.isDefined){
           val attractionActor = attraction.get
           attractionActor ! VisitAttraction( message.ueLog)
        }else{
           logger.error("The object in the cell is not an attraction actor")
        }
      }
      case message => logger.info("received unknown message "+message)
    }
}
