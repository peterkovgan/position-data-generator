package algos

import core.Cell
import org.slf4j.LoggerFactory
import scalax.collection.Graph
import scalax.collection.edge.WUnDiEdge
import scala.collection.mutable
import scala.collection.mutable.Buffer

case class ShortPathCreator(graph:Graph[Int,WUnDiEdge], cell1:Cell, cell2:Cell, map:mutable.Map[Int, Cell]) {
  val logger = LoggerFactory.getLogger("algos.ShortPathCreator")

  def create():mutable.Buffer[(Int,Int)]  ={
    val pathElements:mutable.Buffer[(Int,Int)] = Buffer.empty
    val nodeT1 =  graph get cell1.getVertex
    val nodeT2 =  graph get cell2.getVertex

    //logger.info("Shortest Path:"+cell1.x+","+cell1.y+":"+cell2.x+","+cell2.y)

    val traversal = nodeT1.outerNodeTraverser.shortestPathTo(nodeT2)
    val graphElements = traversal.getOrElse(null)
    if(graphElements != null){
      graphElements.foreach{
        element=>{
          if(!element.toString.contains("~")) {
            val secondVertex = element.toString.toInt
            val cellOption = map.get(secondVertex)
            if (cellOption.isDefined) {
              //logger.info("Shortest Path Cell:"+cellOption.get.x+":"+ cellOption.get.y)
              pathElements.addOne((cellOption.get.x, cellOption.get.y))
            }
          }
        }
      }
      pathElements.remove(0)
      pathElements.remove(pathElements.size-1)
    }else{
      logger.error("No short path found from "+cell1+": to "+ cell2)
    }
    pathElements
  }

}
