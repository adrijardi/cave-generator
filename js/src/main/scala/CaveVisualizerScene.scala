import ThreeUtils._
import cave.{MarchingSquares, Square}
import org.denigma.threejs._
import org.denigma.threejs.extensions.Container3D
import org.scalajs.dom.raw.HTMLElement

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Promise}
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js.Array
import scala.scalajs.js.JSConverters._

class CaveVisualizerScene(
  val container: HTMLElement,
  val width: Double,
  val height: Double,
  data: immutable.Vector[immutable.Vector[Boolean]],
  val nodeSize: Double = 1d,
  val wallHeight: Double = 2d,
  val textureOffset: (Double, Double) = (0, 0),
  val textureScale: (Double, Double) = (10, 10),
) extends Container3D {

  override def distance: Double = 200
  val caveHeight                = data.length
  val caveWidth                 = data.head.length

  private val topologies: List[Square.Topology[Vector3]] = MarchingSquares(data, nodeSize, new Vector3(0, 0, 0))
  val vertices: List[List[Vector3]]                      = topologies.map(_.vertices)

  implicit val ec: ExecutionContext = JSExecutionContext.queue

  val texturePromise = Promise[Texture]()

  new TextureLoader().load("textures/walls.jpg", texturePromise.success)

  val light = new DirectionalLight(0xffffff, 2)
  light.position.set(100, 100, -100) //.normalize()
  scene.add(light)

  val light2 = new DirectionalLight(0x6699ff, 1)
  light.position.set(.5, 3, 1).normalize()
  scene.add(light2)

  val topWall: Geometry = mergeGeometries {
    vertices.flatMap((vertices: List[Vector3]) => createEasyGeometry(vertices))
  }

  val wallGeometries: Geometry = mergeGeometries {
    topologies
      .flatMap(_.walls)
      .map(sideWallMesh)
      .flatMap { vertices =>
        createGeometry(vertices, List(new Face3(0, 1, 2), new Face3(2, 3, 0)))
      }
  }

  val floorMesh: Option[Geometry] = createEasyGeometry(
    List(
      new Vector3(-caveWidth / 2, -caveHeight / 2, -wallHeight),
      new Vector3(caveWidth / 2, -caveHeight / 2, -wallHeight),
      new Vector3(caveWidth / 2, caveHeight / 2, -wallHeight),
      new Vector3(-caveWidth / 2, caveHeight / 2, -wallHeight),
    )
  )

  texturePromise.future.map { texture =>
    texture.wrapS = THREE.RepeatWrapping
    texture.wrapT = THREE.RepeatWrapping

    val wallsTopMaterial = new MeshStandardMaterial(materialParams(0xffffff, texture = Some(texture)))
    val wallsMaterial    = new MeshStandardMaterial(materialParams(0x666666))
    val floorMaterial    = new MeshStandardMaterial(materialParams(0x666666, THREE.DoubleSide, texture = Some(texture)))

    scene.add(new Mesh(topWall, wallsTopMaterial))
    scene.add(new Mesh(wallGeometries, wallsMaterial))
    floorMesh.foreach(g => scene.add(new Mesh(g, floorMaterial)))
  }

  private def createEasyGeometry(vertices: List[Vector3]): Option[Geometry] =
    vertices match {
      case Nil => None
      case _ =>
        val triangles = THREE.ShapeUtils.triangulateShape(vertices.toJSArray, Nil.toJSArray)
        val faces     = triangles.map(tri => new Face3(tri(0), tri(1), tri(2)))
        val uvs = triangles.map { face =>
          def vertFn(index: Int) = {
            val vertex = vertices(index) // TODO ineficient
            new Vector2(
              (vertex.x + textureOffset._1) / textureScale._1,
              (vertex.y + textureOffset._2) / textureScale._2
            )
          }

          face.map(vertFn)
        }
        createGeometry(vertices, faces.toList, uvs)
    }

  private def createGeometry(
    vertices: List[Vector3],
    faces: List[Face3],
    uvs: Array[Array[Vector2]] = Array(),
  ): Option[Geometry] =
    vertices match {
      case Nil => None
      case _ =>
        val geom = new Geometry()
        geom.vertices = vertices.toJSArray
        geom.faces = faces.toJSArray
        geom.faceVertexUvs = List(uvs.toJSArray).toJSArray
        geom.computeFaceNormals()
        Some(geom)
    }

  private def sideWallMesh(side: (Vector3, Vector3)): List[Vector3] = List(
    side._1,
    side._2,
    new Vector3(side._2.x, side._2.y, -wallHeight),
    new Vector3(side._1.x, side._1.y, -wallHeight),
  )

}
