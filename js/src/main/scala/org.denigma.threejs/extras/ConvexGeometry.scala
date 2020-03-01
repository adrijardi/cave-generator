package org.denigma.threejs.extras

import org.denigma.threejs.{Geometry, Vector3}

import scala.scalajs.js
import scala.scalajs.js.Array
import scala.scalajs.js.annotation.JSName

@js.native
@JSName("THREE.ConvexGeometry")
class ConvexGeometry(points: Array[Vector3]) extends Geometry {}
