package core

import java.util

import scala.collection.mutable
import scala.util.Random

/**
 *
 * This object exists all the time the UeActor exists
 * It is created, when UeActor creates its profile
 * So all the data in the constructor / body of the object is the same for all days - those are constants
 *
 *
 */
case class UeTimeInfo(startTimestamp:Long,
                      dailyStartUeFluctuationMinutes:Int,
                      roundTripHours:Int,
                      roundTripHoursMin:Int,
                      cellTimeTypesCrossingCellSec:util.List[Integer],
                      cellTimeTypesCrossingCellSecRandomPercent:Int,
                      attractionsPath: mutable.Buffer[String],
                      mixTransportBetweenDays:Boolean){

         val MILLS_IN_24_HOURS=60*60*24*1000
         val random = new Random(this.hashCode())
         //a bit randomized daily start
         var dayStartTimestamp:Long = 0L
         val dailyAttractionsNumber = attractionsPath.size -1
         var currentVisited = 0
         var currentTransportType=0

         def howMuchTimeToSpend(roundTripHours: Int, dailyAttractionsNumber: Int):mutable.Map[Int,(Long, Long)] = {
            val map: mutable.Map[Int,(Long, Long)] = mutable.Map.empty
            val millsPerLocationInAverageDistributedEquallyMin = roundTripHoursMin * 60 * 60 * 1000 / dailyAttractionsNumber
            val millsPerLocationInAverageDistributedEqually    = roundTripHours * 60 * 60 * 1000 / dailyAttractionsNumber
            for(i<-0 until dailyAttractionsNumber){
                 val minTimeSpent = random.nextLong(millsPerLocationInAverageDistributedEquallyMin)
                 val maxTimeSpent = minTimeSpent + random.nextLong(millsPerLocationInAverageDistributedEqually)
                 map.put(i,(minTimeSpent,maxTimeSpent))
            }
            map
         }

         val dailyAttractionsTimeSpentWithRandom = howMuchTimeToSpend(roundTripHours, dailyAttractionsNumber)


         var dailyAttractionsTimeSpentThisDayOnly:mutable.Map[Int,Long] = null

         //two algorithms
         def randomize(dailyAttractionsTimeSpentWithRandom: mutable.Map[Int, (Long, Long)]): mutable.Map[Int, Long] = {
              val map: mutable.Map[Int,Long] = mutable.Map.empty
              val randoms = new Random(System.currentTimeMillis())
              dailyAttractionsTimeSpentWithRandom.foreach { pair =>
                    map.put(pair._1, pair._2._1 + randoms.nextLong(pair._2._2-pair._2._1))//some random between min and max
              }
              map
         }

         //set all start parameters of the day
         def moveOneDay(day:Int): Unit ={
              currentVisited = 0
              currentTransportType = 0
              dayStartTimestamp = startTimestamp+(day*MILLS_IN_24_HOURS) + random.nextInt(dailyStartUeFluctuationMinutes)*60*1000
              dailyAttractionsTimeSpentThisDayOnly = randomize(dailyAttractionsTimeSpentWithRandom)
         }

         def getFirstAttractionTime:Long = dayStartTimestamp


         def getLocationTimes(pathElements: mutable.Buffer[(Int, Int)]): mutable.Buffer[Long] = {

              val times: mutable.Buffer[Long] = mutable.Buffer.empty
              var transportType = 0

              if(mixTransportBetweenDays) {//random transport
                  transportType = random.nextInt(cellTimeTypesCrossingCellSec.size())
              }else {
                  transportType = currentTransportType
                  if ( currentTransportType < cellTimeTypesCrossingCellSec.size() - 1 ){
                      currentTransportType += 1
                  }else{
                      currentTransportType = 0
                  }
              }

              val millsInCell = cellTimeTypesCrossingCellSec.get(transportType)*1000
              pathElements.foreach {
                e=>
                val millsInCellRandomPart = {
                     if(cellTimeTypesCrossingCellSecRandomPercent>0) {
                       millsInCell / 100 * random.nextLong(cellTimeTypesCrossingCellSecRandomPercent)
                     }else{
                        0
                     }
                }
                val timeFull = millsInCell + millsInCellRandomPart
                times.addOne(timeFull)
              }
              times
         }

         def getNextAttractionTime:Long = {
             val timeSpent = dailyAttractionsTimeSpentThisDayOnly.get(currentVisited).get
             currentVisited+=1
             timeSpent
         }

}
