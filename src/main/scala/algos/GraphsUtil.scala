package algos

import core.Cell

import scala.collection.mutable
import scalax.collection.immutable.Graph
import scalax.collection.edge.WUnDiEdge

import scala.util.Random


//documentation
//http://www.scala-graph.org/guides/core-traversing.html
object GraphsUtil {

  val MaxWeight = 100
  def constructGraph(grid: Array[Array[Cell]], fluctuations:Option[Fluctuations]): (Graph[Int, WUnDiEdge],mutable.Map[Int,Cell])  = {
    val map = mutable.Map[Int,Cell]()
    val width =  grid.size
    val height = grid(0).size
    connectCellAndVertex(grid, map, width, height)
    val edges = mutable.Set[WUnDiEdge[Int]]()
    val nodes = mutable.Set[Int]()
    connectCellsInColumns(grid, width, height, edges, nodes)
    var applyWalkAroundFluctuations             = false
    var maxLightWeightHorizontalStretchesLength = 0
    var maxLightWeightVertexValue               = 0
    var maxLightWeightHorizontalStretchesProbabilityInARaw = 0
    if(fluctuations.isDefined){
      applyWalkAroundFluctuations             = fluctuations.get.applyWalkAroundFluctuations
      maxLightWeightHorizontalStretchesLength = fluctuations.get.maxLightWeightHorizontalStretchesLength
      maxLightWeightVertexValue               = fluctuations.get.maxLightWeightVertexValue
      maxLightWeightHorizontalStretchesProbabilityInARaw = fluctuations.get.maxLightWeightHorizontalStretchesProbabilityInARaw
    }
    connectCellsInRows(grid, width, height, edges, nodes, applyWalkAroundFluctuations, maxLightWeightHorizontalStretchesLength, maxLightWeightVertexValue, maxLightWeightHorizontalStretchesProbabilityInARaw)
    (Graph.from(nodes, edges), map)
  }

  private def connectCellAndVertex(grid: Array[Array[Cell]], map: mutable.Map[Int, Cell], width: Int, height: Int) = {
    var vertexIndex = 0;
    //init graph, set empty cells
    for (x <- 0 until width) {
      for (y <- 0 until height) {
        var cell = grid(x)(y)
        if (cell == null) {
          cell = new Cell(x, y, false, None)
          grid(x)(y) = cell
        }
        if (!cell.obstacle) {
          var topCell: Cell = null
          var bottomCell: Cell = null
          var leftCell: Cell = null
          var rightCell: Cell = null
          if (y - 1 > 0) {
            topCell = grid(x)(y - 1)
          }
          if (y + 1 < height) {
            bottomCell = grid(x)(y + 1)
          }
          if (x - 1 > 0) {
            leftCell = grid(x - 1)(y)
          }
          if (x + 1 < width) {
            rightCell = grid(x + 1)(y)
          }
          if ((topCell != null && !topCell.obstacle)
            || (bottomCell != null && !bottomCell.obstacle)
            || (leftCell != null && !leftCell.obstacle)
            || (rightCell != null && !rightCell.obstacle)
          ) {
            cell.setVertexIndex(vertexIndex)
            map.addOne((vertexIndex -> cell))
            vertexIndex += 1

          }
        }
      }
    }
  }

  private def connectCellsInRows(grid: Array[Array[Cell]], width: Int, height: Int, edges: mutable.Set[WUnDiEdge[Int]], nodes: mutable.Set[Int], applyWalkAroundFluctuations: Boolean, maxLightWeightHorizontalStretchesLength: Int, maxLightWeightVertexValue: Int, maxLightWeightHorizontalStretchesProbabilityInARaw: Int) = {
    for (y <- 0 until height) {

      val randomLightStretchStart = (new Random).nextInt(width)
      val lightStretchEnd = randomLightStretchStart + maxLightWeightHorizontalStretchesLength

      for (x <- 1 until width) {
        val cellLeft = grid(x - 1)(y)
        val cellRight = grid(x)(y)
        if (!cellLeft.obstacle) {
          if (!cellRight.obstacle) {
            val vertexLeft = cellLeft.getVertex
            val vertexRight = cellRight.getVertex
            val weight = if (applyWalkAroundFluctuations) getLightWeight(x, maxLightWeightHorizontalStretchesProbabilityInARaw, randomLightStretchStart, lightStretchEnd, maxLightWeightVertexValue, MaxWeight) else MaxWeight
            val edge = WUnDiEdge(vertexLeft, vertexRight)(weight)
            edges += edge
            nodes += vertexLeft
            nodes += vertexRight
          }
        }
      }
    }
  }

  private def connectCellsInColumns(grid: Array[Array[Cell]], width: Int, height: Int, edges: mutable.Set[WUnDiEdge[Int]], nodes: mutable.Set[Int]) = {
    for (x <- 0 until width) {
      for (y <- 1 until height) {
        val cellLower = grid(x)(y - 1)
        val cellHigher = grid(x)(y)
        if (!cellLower.obstacle) {
          if (!cellHigher.obstacle) {
            val vertexLower = cellLower.getVertex
            val vertexHigher = cellHigher.getVertex
            val edge = WUnDiEdge(vertexLower, vertexHigher)(MaxWeight)
            edges += edge
            nodes += vertexLower
            nodes += vertexHigher
          }
        }
      }
    }
  }

  def getLightWeight(c: Int, maxLightWeightHorizontalStretchesProbabilityInARaw:Int, randomLightStretchStart: Int, lightStretchEnd: Int, maxLightWeightVertexValue:Int, MaxWeight: Int) = {
      if((new Random).nextInt(100) > 100 - maxLightWeightHorizontalStretchesProbabilityInARaw){
          if(c>=randomLightStretchStart && c<=lightStretchEnd){
            maxLightWeightVertexValue
          }else{
            MaxWeight
          }
      }else{
        MaxWeight
      }
  }
}
