package org.denigma.threejs.extensions

import org.denigma.threejs.{PerspectiveCamera, Renderer, Scene, WebGLRenderer}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement

trait SceneContainer {

  def container: HTMLElement

  def width: Double

  def height: Double

  type RendererType <: Renderer

  lazy val scene = new Scene()

  def distance: Double = 2000

  lazy val renderer: RendererType = this.initRenderer()

  lazy val camera = initCamera()

  def aspectRatio: Double = width / height

  var rendering = true

  def stopRender() = {
    rendering = false
    renderer.asInstanceOf[WebGLRenderer].clear(true, true, true) // TODO dirty
  }

  protected def initRenderer(): RendererType

  protected def initCamera() = {
    val fov    = 40
    val near   = 1
    val far    = 1000000
    val camera = new PerspectiveCamera(fov, this.aspectRatio, near, far)
    camera.position.z = distance
    camera
  }

  protected def onEnterFrameFunction(double: Double): Unit =
    if (rendering) {
      onEnterFrame()
      render()
    }

  def onEnterFrame(): Unit =
    renderer.render(scene, camera)

  private def render(): Int = dom.window.requestAnimationFrame(onEnterFrameFunction _)

  def startRender(): Int = render()

}
