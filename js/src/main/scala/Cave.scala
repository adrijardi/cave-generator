import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js.annotation.JSExport

@JSExport("Cave")
object Cave extends scalajs.js.JSApp {
  lazy val elem: HTMLElement = dom.document.body

  @JSExport
  def main(): Unit = {
//    this.bindView()

    val gen = CaveGenerator.create((100, 100), 3)

//    val gen = Vector(
//      Vector(true, true),
//      Vector(true, true),
//      Vector(true, true),
//      Vector(true, true)
//    )
    val container =
      dom.document.getElementById("container").asInstanceOf[HTMLElement]
    new CaveVisualizerScene(
      container,
      container.clientWidth,
      container.clientWidth,
      gen
    ).render()
  }
}
