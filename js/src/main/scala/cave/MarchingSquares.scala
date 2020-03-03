package cave

import cave.Square.Topology

import scala.collection.immutable

object MarchingSquares {

  def apply[Pos](data: immutable.Vector[immutable.Vector[Boolean]], nodeSize: Double, origin: Pos)(
    implicit transform: Transform3D[Pos]
  ): List[Topology[Pos]] = {
    type Row = (ControlNode[Pos], ControlNode[Pos])
    val offset = transform.add(origin, transform.pos(-data.head.length / 2 * nodeSize, data.length / 2 * nodeSize, 0))

    val dataWithIndex: immutable.Vector[(immutable.Vector[(Boolean, Int)], Int)] = data.map(_.zipWithIndex).zipWithIndex

    val controlNodes: scala.Vector[scala.Vector[ControlNode[Pos]]] =
      mapData(dataWithIndex) {
        case (x, y, on) =>
          val relativePosition = transform.pos(x * nodeSize, y * nodeSize * -1, 0)
          val absolutePosition = transform.add(relativePosition, offset)
          ControlNode(absolutePosition, on, nodeSize)
      }

    val slidingRows: scala.Vector[scala.Vector[Row]] =
      controlNodes.map(Utils.slidePairs)

    val squares = slidingRows
      .sliding(2)
      .collect {
        case it if it.length == 2 => it.head.zip(it(1))
      }
      .flatten
      .map {
        case ((topLeft, topRight), (bottomLeft, bottomRight)) =>
          new Square(topLeft, topRight, bottomRight, bottomLeft)
      }
      .toList

    squares.map(_.vertices)
  }

  private type UpdateFn[A, O] = ((Int, Int, A)) => O

  private def mapData[A, O](
    data: immutable.Vector[(immutable.Vector[(A, Int)], Int)]
  )(fn: UpdateFn[A, O]): scala.Vector[scala.Vector[O]] =
    data.map {
      case (row, y) =>
        row.map {
          case (a, x) =>
            fn(x, y, a)
        }
    }
}
