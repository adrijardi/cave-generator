package cave

import cave.Square.Topology

class Square[Pos](
  topLeft: ControlNode[Pos],
  topRight: ControlNode[Pos],
  bottomRight: ControlNode[Pos],
  bottomLeft: ControlNode[Pos]
) {

  def centreTop: Node[Pos]    = topLeft.right
  def centreLeft: Node[Pos]   = bottomLeft.above
  def centreRight: Node[Pos]  = bottomRight.above
  def centerBottom: Node[Pos] = bottomLeft.right

  def vertices: Topology[Pos] = {
    val t = true
    val f = false

    (topLeft.active, topRight.active, bottomRight.active, bottomLeft.active) match {
      case (`f`, `f`, `f`, `f`) => Topology(Nil, Nil)

      case (`f`, `f`, `f`, `t`) =>
        Topology(List(centreLeft, centerBottom, bottomLeft), List((centreLeft, centerBottom)))
      case (`f`, `f`, `t`, `f`) =>
        Topology(List(bottomRight, centerBottom, centreRight), List((centerBottom, centreRight)))
      case (`f`, `t`, `f`, `f`) => Topology(List(topRight, centreRight, centreTop), List((centreRight, centreTop)))
      case (`t`, `f`, `f`, `f`) => Topology(List(topLeft, centreTop, centreLeft), List((centreTop, centreLeft)))

      case (`f`, `f`, `t`, `t`) =>
        Topology(List(centreLeft, centreRight, bottomRight, bottomLeft), List((centreLeft, centreRight)))
      case (`f`, `t`, `t`, `f`) =>
        Topology(List(bottomRight, centerBottom, centreTop, topRight), List((centerBottom, centreTop)))
      case (`t`, `f`, `f`, `t`) =>
        Topology(List(topLeft, centreTop, centerBottom, bottomLeft), List((centreTop, centerBottom)))
      case (`t`, `t`, `f`, `f`) =>
        Topology(List(topLeft, topRight, centreRight, centreLeft), List((centreRight, centreLeft)))

      case (`f`, `t`, `f`, `t`) =>
        Topology(
          List(centreLeft, centreTop, topRight, centreRight, centerBottom, bottomLeft),
          List((centreLeft, centreTop), (centreRight, centerBottom))
        )
      case (`t`, `f`, `t`, `f`) =>
        Topology(
          List(topLeft, centreTop, centreRight, bottomRight, centerBottom, centreLeft),
          List((centreTop, centreRight), (centerBottom, centreLeft))
        )

      case (`f`, `t`, `t`, `t`) =>
        Topology(List(topRight, bottomRight, bottomLeft, centreLeft, centreTop), List((centreLeft, centreTop)))
      case (`t`, `f`, `t`, `t`) =>
        Topology(List(topLeft, centreTop, centreRight, bottomRight, bottomLeft), List((centreTop, centreRight)))
      case (`t`, `t`, `f`, `t`) =>
        Topology(List(topLeft, topRight, centreRight, centerBottom, bottomLeft), List((centreRight, centerBottom)))
      case (`t`, `t`, `t`, `f`) =>
        Topology(List(topLeft, topRight, bottomRight, centerBottom, centreLeft), List((centerBottom, centreLeft)))

      case (`t`, `t`, `t`, `t`) => Topology(List(topLeft, topRight, bottomRight, bottomLeft), Nil)

      case other =>
        println(s"Error: Square $other not matched")
        Topology(Nil, Nil)
    }
  }.map(_.position)

}

object Square {
  final case class Topology[Pos](vertices: List[Pos], walls: List[(Pos, Pos)]) {

    def map[O](fn: Pos => O): Topology[O] = {
      val tupleFn: ((Pos, Pos)) => (O, O) = t => (fn(t._1), fn(t._2))
      Topology(vertices.map(fn), walls.map(tupleFn))
    }
  }
}
