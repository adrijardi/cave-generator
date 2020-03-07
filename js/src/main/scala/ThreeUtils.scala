import org.denigma.threejs.{Color, Geometry, Matrix4, MeshStandardMaterialParameters, THREE, Texture}

import scala.scalajs.js

object ThreeUtils {

  def materialParams(
    color: Int,
    sides: org.denigma.threejs.Side = THREE.FrontSide,
    texture: Option[Texture] = None,
    wireframe: Boolean = false,
  ): MeshStandardMaterialParameters =
    js.Dynamic
      .literal(
        color = new Color(color),
        wireframe = wireframe,
        side = sides,
        map = texture.orNull
      )
      .asInstanceOf[MeshStandardMaterialParameters]

  def mergeGeometries(geom: List[Geometry]): Geometry = {
    val result = geom.reduce((a, b) => {
      a.merge(b, new Matrix4(), 0)
      a
    })
    result.mergeVertices()
    result
  }

}
