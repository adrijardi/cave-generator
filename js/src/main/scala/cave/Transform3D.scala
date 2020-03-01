package cave

import org.denigma.threejs.Vector3

trait Transform3D[Pos] {
  def add(pos: Pos)(x: Double, y: Double, z: Double): Pos
  def zero: Pos
}

object Transform3D {
  implicit object Vector3Pos extends Transform3D[Vector3] {
    override def add(position: Vector3)(x: Double, y: Double, z: Double): Vector3 =
      new Vector3(position.x +x , position.y + y, position.z + z)

    override def zero: Vector3 = new Vector3(0, 0, 0)
  }
}
