package core.messages

import akka.actor.ActorRef
import core.UeLog

case class RegisterDayTrip(ueLog:UeLog, ue:ActorRef)
