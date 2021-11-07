package core

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import core.messages.{CreateUe, StartTheSimulation, UeIsReady, UeStartMove}
import org.slf4j.LoggerFactory

import scala.collection.mutable.Buffer

object StartActor{
  def props(grid:ActorRef, registry:ActorRef): Props = Props(new StartActor(grid, registry))
}
class StartActor(grid:ActorRef, registry:ActorRef) extends Actor{

  val logger = LoggerFactory.getLogger("core.StartActor")
  val config = ConfigFactory.load
  var ues = config.getInt("akka.ue.profiles")
  val profilesBuffer:Buffer[ActorRef] = Buffer.empty


  def receive = {
    case StartTheSimulation => {
         createProfiles(context.system)
         startUes
    }
    case message: UeIsReady=> {
        message.ue ! UeStartMove
    }
    case _ => logger.info("received unknown message")
  }

  def createProfiles(system:ActorSystem): Unit ={
     for( i<-0 to ues){
        val ue  = system.actorOf(UeActor.props(grid, registry, i),"ue"+i)
        profilesBuffer.addOne(ue)
     }
  }
  def startUes: Unit ={
     for( i<-0 until ues){
        val ueProfile = profilesBuffer(i)
        ueProfile ! CreateUe
     }
  }

}
