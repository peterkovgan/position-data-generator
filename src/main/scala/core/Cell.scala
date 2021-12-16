package core

import akka.actor.ActorRef

/**
 * This object represents the cell on the grid
 */
class Cell(val x:Int, val y:Int, val obstacle:Boolean, val attraction: Option[ActorRef] ) {
  def getVertex = vertexIndex

  var vertexIndex = 0;
  def setVertexIndex(vertexIndex: Int) = {
     this.vertexIndex = vertexIndex
  }
}
