package core.messages

import core.UeLog

case class GetShortPath(visitedBeforeX:Int, visitedBeforeY:Int, x:Int,y:Int, attrId:Int, ueLog:UeLog)
