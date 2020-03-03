package cave

import org.denigma.threejs.Vector3

trait Transform3D[Pos] {
  def add(posA: Pos, posB: Pos): Pos
  def pos(x: Double, y: Double, z: Double): Pos
  def zero: Pos
}

object Transform3D {
  implicit object Vector3Pos extends Transform3D[Vector3] {
    override def add(posA: Vector3, posB: Vector3): Vector3 =
      new Vector3(posA.x + posB.x, posA.y + posB.y, posA.z + posB.z)

    override def pos(x: Double, y: Double, z: Double): Vector3 = new Vector3(x, y, z)

    override def zero: Vector3 = new Vector3(0, 0, 0)
  }
}
