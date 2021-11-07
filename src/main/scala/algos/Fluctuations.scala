package algos

import akka.actor.ActorRef
import com.typesafe.config.ConfigFactory
import core.{LogElement, UeLog}

import scala.collection.mutable
import scala.collection.mutable.{Buffer, Map}
import scala.util.Random

object Fluctuations {
    def apply(logsPerUe: Map[ActorRef, Buffer[UeLog]]) = {
         new Fluctuations(logsPerUe)
    }
}

class Fluctuations(logsPerUe: Map[ActorRef,Buffer[UeLog]]){

    def getObstacles:Buffer[(Int,Int)] = obstaclesToAdd
    def getUeProfiles = logsPerUe.keys

    val obstaclesToAdd:Buffer[(Int,Int)] =  Buffer.empty
    val config = ConfigFactory.load()

    val gridWidth                                =  config.getInt("akka.grid.width")
    val gridHeight                               =  config.getInt("akka.grid.height")
    //random obstacles changing the shortest path
    val applyObstacleBasedFluctuations           =  config.getBoolean("akka.fluctuations.applyObstacleBasedFluctuations")
    val maxPercentagePathsToFluctuate            =  config.getInt("akka.fluctuations.maxPercentagePathsToFluctuate")
    val maxObstaclesPerPath                      =  config.getInt("akka.fluctuations.maxObstaclesPerPath")
    val maxObstacleWidth                         =  config.getInt("akka.fluctuations.maxObstacleWidth")

    //random walk-around, that prolongs the shortest path
    def applyWalkAroundFluctuations              = config.getBoolean("akka.fluctuations.applyWalkAroundFluctuations")
    def maxLightWeightHorizontalStretchesLength  = config.getInt("akka.fluctuations.maxLightWeightHorizontalStretchesLength")
    def maxLightWeightVertexValue                = config.getInt("akka.fluctuations.maxLightWeightVertexValue")
    def maxLightWeightHorizontalStretchesProbabilityInARaw = config.getInt("akka.fluctuations.maxLightWeightHorizontalStretchesProbabilityInARaw")



    if(applyObstacleBasedFluctuations) {
        obstacleBasedFluctuations
    }

    private def obstacleBasedFluctuations = {
      val random = new Random(System.currentTimeMillis())
      for (value <- logsPerUe.values) {

        if (random.nextInt(100) <= maxPercentagePathsToFluctuate) {
          val initialUeLog = value(0)
          val logVisitedFirstTime = initialUeLog.allVisited
          val maxObstacles = random.nextInt(maxObstaclesPerPath)
          for (i <- 0 to maxObstacles) {
            createObstacle(random, logVisitedFirstTime)
          }
        }
      }
    }

    private def createObstacle(random: Random, logVisitedFirstTime: mutable.Buffer[LogElement]) = {
      val indexOfTheNewObstacleCell = random.nextInt(logVisitedFirstTime.size)
      val cellContentLog = logVisitedFirstTime(indexOfTheNewObstacleCell)
      if (cellContentLog.attractionId == -1) { //just a cell, not an attraction
        val obstacleWidthPreliminary = random.nextInt(maxObstacleWidth)
        val obstacleWidthReal = if (obstacleWidthPreliminary != 0) obstacleWidthPreliminary else 1
        val x = cellContentLog.x
        val y = cellContentLog.y
        if (x + obstacleWidthReal < gridWidth) {
          for (i <- 0 to obstacleWidthReal) {
            obstaclesToAdd.addOne((x + i, y))
          }
        } else if (x - obstacleWidthReal > 0) {
          for (i <- 0 to obstacleWidthReal) {
            obstaclesToAdd.addOne((x - i, y))
          }
        }
      }
    }
}
