package core

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.FileWriter
import java.util.{Calendar, Date}

import algos.Fluctuations
import core.messages.{ChangeGrid, RegisterDayTrip}

import scala.collection.mutable.Map
import scala.collection.mutable.Buffer
import scala.math.Ordering.Implicits.infixOrderingOps

/**
 * This actor triggered by evey UE, when the daily walk ends
 * This actor collects daily data, prints it in CSV files
 * This actor also re-initializes the grid (to make tomorrow grid look a bit different)
 *
 */
object CycleManagerActor{
    def props(grid:ActorRef): Props = Props(new CycleManagerActor(grid))
}

class CycleManagerActor(val grid:ActorRef) extends Actor{
  val logger   = LoggerFactory.getLogger("core.AmfRegistrator")
  var daysCompletedPerUe:Map[ActorRef, Int] = Map()
  val config   = ConfigFactory.load()
  val maxDays  = config.getInt("akka.experiment.days")
  val ueNumber = config.getInt("akka.ue.profiles")
  val oneFile  = config.getBoolean("akka.output.oneFile")


  var currentNumberOfUeReported = 0
  var daysCompleted=0
  var oneFileHeader=false



  def storeAllInOne(ueLog: UeLog, ue:ActorRef) = {
    var daysCompleted = 1
    if(daysCompletedPerUe.contains(ue)){
      daysCompleted = daysCompletedPerUe.get(ue).get
    }
    val writer = new BufferedWriter(new FileWriter(config.getString("akka.output.file"), true))
    daysCompleted+=1
    daysCompletedPerUe.put(ue, daysCompleted)
    if(!oneFileHeader) {
      writer.write("attr,x,y,ueId,cell,timePeriod,hour,minute\n")
      oneFileHeader = true
    }

    val first:LogElement = ueLog.allVisited.head
    val rest:Buffer[LogElement]=ueLog.allVisited.tail
    var time = first.locationTime

    val date = new Date(time)
    val  calendar = Calendar.getInstance()
    calendar.setTime(date)
    val hour   = calendar.get(Calendar.HOUR_OF_DAY);
    val minute = calendar.get(Calendar.MINUTE);


    writer.write(first.attractionId+","+first.x+","+first.y+","+first.ueId+","+first.cellId+","+first.locationTime+","+ hour+","+minute+"\n")
    rest.foreach(e=> {
      time+=e.locationTime
      val date = new Date(time)
      val  calendar = Calendar.getInstance()
      calendar.setTime(date)
      val hour   = calendar.get(Calendar.HOUR_OF_DAY);
      val minute = calendar.get(Calendar.MINUTE);
      writer.write(e.attractionId+","+e.x+","+e.y+","+e.ueId+","+e.cellId+","+time+","+ hour+","+minute+"\n")
    })

    writer.flush()
    writer.close
  }

  def storeDayTrip(ueLog: UeLog, ue:ActorRef) = {
     var daysCompleted = 1
     if(daysCompletedPerUe.contains(ue)){
         daysCompleted = daysCompletedPerUe.get(ue).get
     }
     val writer = new BufferedWriter(new FileWriter("day_"+daysCompleted + "_UE" + ueLog.ueId+ "_" + config.getString("akka.output.file")))
     daysCompleted+=1
     daysCompletedPerUe.put(ue, daysCompleted)
     writer.write("attr,x,y,ueId,cell,timePeriod\n")
     ueLog.allVisited.foreach(e=> writer.write(e.attractionId+","+e.x+","+e.y+","+e.ueId+","+e.cellId+","+e.locationTime+"\n"))
     writer.flush()
     writer.close
  }

  def storeDayTripFromSummary(ueLog: UeLog, ue: ActorRef) = {
    var daysCompleted = 1
    if(daysCompletedPerUe.contains(ue)){
      daysCompleted = daysCompletedPerUe.get(ue).get
    }
    val writer = new BufferedWriter(new FileWriter("day_"+daysCompleted + "_UE" + ueLog.ueId+ "_" + config.getString("akka.output.file")))
    daysCompleted+=1
    daysCompletedPerUe.put(ue, daysCompleted)
    writer.write("x,y,ueId,cell,lon,lat,timePeriod\n")
    ueLog.allVisited.foreach(e=> {
         val row = Main.geoSummary.get.mapProvenAttractionsByCoordinates.get((e.x, e.y))
         if(row.isDefined){
              val geoArray = row.get
              writer.write(e.x+","+e.y+","+e.ueId+","+geoArray(0)+","+geoArray(3)+","+geoArray(4)+","+e.locationTime+"\n")
         }
    })
    writer.flush()
    writer.close

  }

  var logsPerUe:Map[ActorRef, Buffer[UeLog]] = Map()



  override def receive = {

        case message:RegisterDayTrip => {
              val ue=message.ue
              val ueLog=message.ueLog
              if(Main.geoSummary.isDefined){
                  storeDayTripFromSummary(ueLog, ue)
              }else{
                  if(oneFile){
                      storeAllInOne(ueLog, ue)
                  }else{
                      storeDayTrip(ueLog, ue)
                  }
              }

              currentNumberOfUeReported+=1
              if(!logsPerUe.contains(ue)){//we only memorize the first trip now
                  logsPerUe.put(ue, Buffer.empty.addOne(ueLog))
              }
              if(currentNumberOfUeReported==ueNumber){
                  currentNumberOfUeReported=0
                  daysCompleted+=1
                  logger.info("Day "+daysCompleted+" finished")
                  if(daysCompleted < maxDays) {
                    grid ! ChangeGrid(Fluctuations(logsPerUe))
                  }
              }
        }
  }
}

