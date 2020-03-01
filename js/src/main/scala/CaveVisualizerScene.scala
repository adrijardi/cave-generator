import cave.MarchingSquares
import org.denigma.threejs.{Color, DirectionalLight, Face3, Geometry, Mesh, MeshLambertMaterial, MeshLambertMaterialParameters, THREE, Vector3}
import org.denigma.threejs.extensions.Container3D
import org.scalajs.dom.raw.HTMLElement

import scala.collection.immutable
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class CaveVisualizerScene(
  val container: HTMLElement,
  val width: Double,
  val height: Double,
  data: immutable.Vector[immutable.Vector[Boolean]]
) extends Container3D {

  override def distance: Double = 200

  val vertices: List[List[Vector3]] = MarchingSquares(data, 1d)

  def materialParams(color: Int): MeshLambertMaterialParameters =
    js.Dynamic
      .literal(
        color = new Color(color) // wireframe = true
      )
      .asInstanceOf[MeshLambertMaterialParameters]

  val material = new MeshLambertMaterial(materialParams(0xffffff))

  val materials = List(
    material,
    new MeshLambertMaterial(materialParams(0xffff00)),
    new MeshLambertMaterial(materialParams(0x00ffcc)),
    new MeshLambertMaterial(materialParams(0xff00ff)),
    new MeshLambertMaterial(materialParams(0xffff66)),
    new MeshLambertMaterial(materialParams(0xff66ff)),
    new MeshLambertMaterial(materialParams(0x66ffff))
  )

  val meshes = vertices.flatMap(meshFn)

  meshes.foreach(scene.add)

  val light = new DirectionalLight(0xffffff, 2)
  light.position.set(1, 1, 1).normalize()
  scene.add(light)

  val light2 = new DirectionalLight(0x6699ff, 1)
  light.position.set(.5, 3, 1).normalize()
  scene.add(light2)

  private def meshFn(vertices: List[Vector3]): Option[Mesh] =
    vertices match {
      case Nil                => None
      case _ =>
        val geom = new Geometry()
        geom.vertices = vertices.toJSArray
        val triangles = THREE.ShapeUtils.triangulateShape( vertices.toJSArray, Nil.toJSArray )
        val faces = triangles.map(tri => new Face3(tri(0), tri(1), tri(2)))
        geom.faces = faces
        Some(new Mesh(geom, material))
    }
}
