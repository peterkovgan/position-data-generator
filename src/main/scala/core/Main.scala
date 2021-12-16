package core

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import core.messages.{CreateGrid, StartTheSimulation}
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal


/**
 * The class to start everything
 */

object Main {

  val logger = LoggerFactory.getLogger("core.Main")

  var geoSummary:Option[GeoSummary] = None

  def main(args: Array[String]): Unit = {

     val system = ActorSystem("DataGenerator")
     val config = ConfigFactory.load()
     val byGeo = config.getBoolean("akka.geo.by_geo")

     try {

        if(byGeo){
            geoSummary = Some(Geo.doAll)
        }


        val grid = system.actorOf(GridActor.props)
        val registry = system.actorOf(CycleManagerActor.props(grid))
        grid ! CreateGrid
        val startActor = system.actorOf(StartActor.props(grid, registry))
        startActor ! StartTheSimulation

     }catch {
        case NonFatal(e) =>
            logger.error("Terminating due to initialization failure.", e)
     }
  }



}
