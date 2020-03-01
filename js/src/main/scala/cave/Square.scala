package cave

class Square[Pos](topLeft: ControlNode[Pos], topRight: ControlNode[Pos], bottomRight: ControlNode[Pos], bottomLeft: ControlNode[Pos]) {

  def centreTop: Node[Pos]    = topLeft.right
  def centreLeft: Node[Pos]   = bottomLeft.above
  def centreRight: Node[Pos]  = bottomRight.above
  def centerBottom: Node[Pos] = bottomLeft.right

  def vertices: List[Pos] = {
    val t = true
    val f = false

    (topLeft.active, topRight.active, bottomRight.active, bottomLeft.active) match {
      case (`f`, `f`, `f`, `f`) => Nil

      case (`f`, `f`, `f`, `t`) => List(centerBottom, bottomLeft, centreLeft)
      case (`f`, `f`, `t`, `f`) => List(bottomRight, centerBottom, centreRight)
      case (`f`, `t`, `f`, `f`) => List(topRight, centreRight, centreTop)
      case (`t`, `f`, `f`, `f`) => List(topLeft, centreTop, centreLeft)

      case (`f`, `f`, `t`, `t`) => List(centreLeft, centreRight, bottomRight, bottomLeft)
      case (`f`, `t`, `t`, `f`) => List(bottomRight, centerBottom, centreTop, topRight)
      case (`t`, `f`, `f`, `t`) => List(topLeft, centreTop, centerBottom, bottomLeft)

      case (`t`, `t`, `f`, `f`) => List(topLeft, topRight, centreRight, centreLeft)
      case (`f`, `t`, `f`, `t`) => List(centreTop, centreLeft, bottomLeft, centerBottom, centreRight, topRight)
      case (`t`, `f`, `t`, `f`) => List(topLeft, centreLeft, centerBottom, bottomRight, centreRight)

      case (`f`, `t`, `t`, `t`) => List(centreTop, topRight, bottomRight, bottomLeft, centreLeft)
      case (`t`, `f`, `t`, `t`) => List(topLeft, centreTop, centreRight, bottomRight, bottomLeft)
      case (`t`, `t`, `f`, `t`) => List(topLeft, topRight, centreRight, centerBottom, bottomLeft)
      case (`t`, `t`, `t`, `f`) => List(topLeft, topRight, bottomRight, centerBottom, centreLeft)

      case (`t`, `t`, `t`, `t`) => List(topLeft, topRight, bottomRight, bottomLeft)

      case other =>
        println(s"Error: Square $other not matched")
        Nil
    }
  }.map(_.position)

}
