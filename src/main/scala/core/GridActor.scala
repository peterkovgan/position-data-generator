package core


import java.util

import akka.actor.{Actor, ActorSystem, Props}
import algos.{Fluctuations, GraphsUtil, ShortPathCreator}
import com.typesafe.config.ConfigFactory
import core.messages._
import org.slf4j.LoggerFactory
import scalax.collection.edge.WUnDiEdge
import scalax.collection.immutable.Graph

import scala.collection.mutable
import scala.collection.mutable.Buffer
import scala.jdk.CollectionConverters._


object GridActor {
  def props(): Props = Props(new GridActor)
}

class GridActor extends Actor {

  val config = ConfigFactory.load()
  var cells = Array.ofDim[Cell](config.getInt("akka.grid.width"), config.getInt("akka.grid.height"))
  val logger = LoggerFactory.getLogger("core.GridActor")
  var graphTuple: (Graph[Int, WUnDiEdge], mutable.Map[Int, Cell]) = null


  def receive = {
    case CreateGrid => {
      placeAttractionsOnGrid(context.system)
      placeObstaclesOnGrid
      logger.info("Start construct graph")
      graphTuple = GraphsUtil.constructGraph(cells, None)
    }
    case message: ChangeGrid => {
      cells = Array.ofDim[Cell](config.getInt("akka.grid.width"), config.getInt("akka.grid.height"))
      placeAttractionsOnGrid(context.system)
      placeObstaclesOnGrid
      logger.info("Apply fluctuations")
      changeTheGrid(message.fluctuations)
      logger.info("Start construct graph")
      graphTuple = GraphsUtil.constructGraph(cells, Some(message.fluctuations))
      message.fluctuations.getUeProfiles.foreach {
        profile => profile ! UeStartMove
      }
    }
    case message: GetShortPath => {
      val nextId = message.attrId
      val nextCoordinates = (message.x, message.y)
      val ueLog = message.ueLog
      val cell1 = cells(message.visitedBeforeX)(message.visitedBeforeY)
      val cell2 = cells(message.x)(message.y)
      val creator = ShortPathCreator(graphTuple._1, cell1, cell2, graphTuple._2)
      val pathElements = creator.create()
      sender ! BuildAttractionPath(pathElements, nextId, nextCoordinates, ueLog)
    }
    case message: GetFirstAttraction => {
      sender ! UeFirstAttraction(cells(message.coordinates._1)(message.coordinates._2))
    }
    case message: GetNextAttraction => {
      sender ! UeNextAttraction(cells(message.coordinates._1)(message.coordinates._2), message.ueLog)
    }
    case _ => {
      logger.info("received unknown message")
    }
  }

  def changeTheGrid(fluctuations: Fluctuations) = {
    if (fluctuations.getObstacles != null && fluctuations.getObstacles.size > 0) {
      addRandomObstacles(fluctuations)
    }
  }

  private def addRandomObstacles(fluctuations: Fluctuations) = {
    for (location <- fluctuations.getObstacles) {
      val cell = cells(location._1)(location._2)
      if (cell == null) { //not an attraction and not an obstacle yet
        cells(location._1)(location._2) = new Cell(location._1, location._2, true, None)
      }
    }
  }

  def placeAttractionsOnGrid(system: ActorSystem): Unit = {
    val attractionLocations = {
      if(Main.geoSummary.isDefined){
          val list = new util.ArrayList[String]
          Main.geoSummary.get.provenLocations.forEach(e=>list.add(e))
          list
      }else{
          config.getStringList("akka.attractions.location")
      }
    }
    placeAttractionsOnGrid(attractionLocations.asScala, system)
  }

  def placeObstaclesOnGrid: Unit = {
    val obstaclesLocations = {
        if(Main.geoSummary.isDefined){
             val list = new util.ArrayList[String]
             Main.geoSummary.get.obstaclesBuffer.forEach(e=>list.add(e))
             list
        }else{
             config.getStringList("akka.obstacles.location")
        }
    }
    placeObstaclesOnGrid(obstaclesLocations.asScala)
  }

  def placeAttractionsOnGrid(attractionLocations: Buffer[String], system: ActorSystem): Unit = {
    for ((location, i) <- attractionLocations.view.zipWithIndex) {
      val coordinates = location.split(",")
      val x = coordinates(0).toInt
      val y = coordinates(1).toInt
      val attractionActor = system.actorOf(AttractionActor.props(i, x, y, context.self))
      cells(x)(y) = new Cell(x, y, false, Some(attractionActor))
    }
  }

  def placeObstaclesOnGrid(obstaclesLocations: Buffer[String]): Unit = {
    for ((location, i) <- obstaclesLocations.view.zipWithIndex) {
      val coordinates = location.split(",")
      val x = coordinates(0).toInt
      val y = coordinates(1).toInt
      cells(x)(y) = new Cell(x, y, true, None)
    }
  }
}
