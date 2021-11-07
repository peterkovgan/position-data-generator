package core

import akka.actor.ActorSystem
import akka.management.scaladsl.AkkaManagement
import core.messages.{CreateGrid, StartTheSimulation}
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal


object Main {

  val logger = LoggerFactory.getLogger("core.Main")

  var geoSummary:Option[GeoSummary] = None

  def main(args: Array[String]): Unit = {

     val system = ActorSystem("DataGenerator")


     try {



       //geoSummary = Some(Geo.doAll)





        //init(system)
        val grid = system.actorOf(GridActor.props)
        val registry = system.actorOf(CycleManagerActor.props(grid))
        grid ! CreateGrid
        val startActor = system.actorOf(StartActor.props(grid, registry))
        startActor ! StartTheSimulation





    }catch {

          case NonFatal(e) =>
            logger.error("Terminating due to initialization failure.", e)
            //system.terminate()

    }
  }

//  def init(system: ActorSystem): Unit = {
//    //AkkaManagement(system).start()
//  }

}
