import cave.{MarchingSquares, Square, Utils}
import org.denigma.threejs.{
  AmbientLight,
  BoxGeometry,
  Color,
  DirectionalLight,
  Face3,
  Geometry,
  Mesh,
  MeshBasicMaterial,
  MeshLambertMaterial,
  MeshLambertMaterialParameters,
  MeshPhongMaterial,
  MeshStandardMaterial,
  MeshStandardMaterialParameters,
  SpotLight,
  THREE,
  Vector3
}
import org.denigma.threejs.extensions.Container3D
import org.scalajs.dom.raw.HTMLElement

import scala.collection.immutable
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSON

class CaveVisualizerScene(
  val container: HTMLElement,
  val width: Double,
  val height: Double,
  data: immutable.Vector[immutable.Vector[Boolean]]
) extends Container3D {

  override def distance: Double = 200
  val caveHeight                = data.length
  val caveWidth                 = data.head.length

  private val topologies: List[Square.Topology[Vector3]] = MarchingSquares(data, 1d, new Vector3(0, 0, 0))
  val vertices: List[List[Vector3]]                      = topologies.map(_.vertices)

  def materialParams(color: Int): MeshStandardMaterialParameters =
    js.Dynamic
      .literal(
        color = new Color(color),
//        emissive = new Color(0x2a0000),
//        wireframe = true
      )
      .asInstanceOf[MeshStandardMaterialParameters]

  val wallsMaterial = new MeshStandardMaterial(materialParams(0x666633))
  val floorMaterial = new MeshStandardMaterial(materialParams(0x333300))

//  val materials = List(
//    material,
//    new MeshLambertMaterial(materialParams(0xffff00)),
//    new MeshLambertMaterial(materialParams(0x00ffcc)),
//    new MeshLambertMaterial(materialParams(0xff00ff)),
//    new MeshLambertMaterial(materialParams(0xffff66)),
//    new MeshLambertMaterial(materialParams(0xff66ff)),
//    new MeshLambertMaterial(materialParams(0x66ffff))
//  )

  val meshes = vertices.flatMap((vertices: List[Vector3]) => createMesh(vertices, wallsMaterial))

  meshes.foreach(scene.add)

  val light = new DirectionalLight(0xffffff, 2)
  light.position.set(100, 100, -100) //.normalize()
  scene.add(light)

  val light2 = new DirectionalLight(0x6699ff, 1)
  light.position.set(.5, 3, 1).normalize()
  scene.add(light2)

//  val geometry = new BoxGeometry(10, 10, 10, 1, 1, 1);
////  val material = new MeshBasicMaterial({ color: 0x00ff00 });
//  val cube = new Mesh(geometry, material);
//  scene.add(cube);

  // Walls

  type Side = (Vector3, Vector3)

  val walls      = topologies.flatMap(_.walls)
  val wallHeight = 2d

  def sideWallMesh(side: Side): List[Vector3] = List(
    side._1,
    side._2,
    new Vector3(side._2.x, side._2.y, -wallHeight),
    new Vector3(side._1.x, side._1.y, -wallHeight),
  )

  val wallMesh = walls.map(sideWallMesh).flatMap { vertices =>
    createMeshRaw(vertices, List(new Face3(0, 1, 2), new Face3(2, 3, 0)), wallsMaterial)
  }
  wallMesh.foreach(scene.add)

  // END walls

  val floorMesh = createMeshRaw(
    List(
      new Vector3(-caveWidth / 2, -caveHeight / 2, -wallHeight),
      new Vector3(caveWidth / 2, -caveHeight / 2, -wallHeight),
      new Vector3(caveWidth / 2, caveHeight / 2, -wallHeight),
      new Vector3(-caveWidth / 2, caveHeight / 2, -wallHeight),
    ),
    List(new Face3(0, 1, 2), new Face3(2, 3, 0)),
    floorMaterial
  )
  floorMesh.foreach(scene.add)

  private def createMesh(vertices: List[Vector3], material: MeshStandardMaterial): Option[Mesh] =
    vertices match {
      case Nil => None
      case _ =>
        val triangles = THREE.ShapeUtils.triangulateShape(vertices.toJSArray, Nil.toJSArray)
        val faces     = triangles.map(tri => new Face3(tri(0), tri(1), tri(2)))
        createMeshRaw(vertices, faces.toList, material)
    }

  private def createMeshRaw(vertices: List[Vector3], faces: List[Face3], material: MeshStandardMaterial): Option[Mesh] =
    vertices match {
      case Nil => None
      case _ =>
        val geom = new Geometry()
        geom.vertices = vertices.toJSArray
        geom.faces = faces.toJSArray
        geom.computeFaceNormals()
        Some(new Mesh(geom, material))
    }
}
