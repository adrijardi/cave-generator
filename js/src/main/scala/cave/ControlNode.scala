package cave

class Node[Pos](val position: Pos)

case class ControlNode[Pos](override val position: Pos, active: Boolean, size: Double)(implicit transform: Transform3D[Pos])
    extends Node(position) {
  def above: Node[Pos] = new Node(transform.add(position)(0, size/2, 0))
  def right: Node[Pos] = new Node(transform.add(position)(size/2, 0, 0))
}
