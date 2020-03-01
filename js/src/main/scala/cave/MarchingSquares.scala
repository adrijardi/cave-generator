package cave

import scala.collection.immutable

object MarchingSquares {

  def apply[Pos](data: immutable.Vector[immutable.Vector[Boolean]], nodeSize: Double)(implicit transform: Transform3D[Pos]): List[List[Pos]] = {
    type Row = (ControlNode[Pos], ControlNode[Pos])

    val dataWithIndex: immutable.Vector[(immutable.Vector[(Boolean, Int)], Int)] = data.map(_.zipWithIndex).zipWithIndex

    val controlNodes: scala.Vector[scala.Vector[ControlNode[Pos]]] =
      mapData(dataWithIndex) {
        case (x, y, on) =>
          ControlNode(transform.add(transform.zero)(x * nodeSize, y * nodeSize * -1, 0), on, nodeSize)
      }

    val slidingRows: scala.Vector[scala.Vector[Row]] = controlNodes.map {
      _.sliding(2).collect {
        case it if it.length == 2 => (it.head, it(1))
      }.toVector
    }

      val squares = slidingRows.sliding(2)
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
