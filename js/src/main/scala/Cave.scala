import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.util.Try

object Cave {
  lazy val elem: HTMLElement = dom.document.body

  case class Parameters(
    width: Int = 25,
    height: Int = 20,
    wallMargin: Int = 3,
    smoothSteps: Int = 3,
    minRoomSize: Int = 0,
    createPassages: Boolean = true,
  )

  def main(args: Array[String]): Unit = {
    val container =
      dom.document.getElementById("container").asInstanceOf[HTMLElement]

    var visualizer = createCave(container, Parameters())
    visualizer.startRender()

    dom.document
      .getElementById("refresh")
      .addEventListener(
        "click",
        (e: Event) => {
          e.preventDefault()
          visualizer.stopRender()
          visualizer = createCave(container, getParameters)
          visualizer.startRender()
        },
        useCapture = false
      )

  }

  private def getParameters: Parameters = {
    val defaults = Parameters()

    Parameters(
      Try(dom.document.getElementById("width").asInstanceOf[html.Input].value.toInt).getOrElse(defaults.width),
      Try(dom.document.getElementById("height").asInstanceOf[html.Input].value.toInt).getOrElse(defaults.height),
      Try(dom.document.getElementById("wallsMargin").asInstanceOf[html.Input].value.toInt)
        .getOrElse(defaults.wallMargin),
      Try(dom.document.getElementById("smoothSteps").asInstanceOf[html.Input].value.toInt)
        .getOrElse(defaults.smoothSteps),
      Try(dom.document.getElementById("minRoomSize").asInstanceOf[html.Input].value.toInt)
        .getOrElse(defaults.minRoomSize),
      Try(dom.document.getElementById("createPassages").asInstanceOf[html.Input].checked)
        .getOrElse(defaults.createPassages),
    )
  }

  private def createCave(container: HTMLElement, parameters: Parameters) = {
    val gen = CaveGenerator.create(
      (parameters.height, parameters.width),
      parameters.smoothSteps,
      parameters.wallMargin,
      parameters.minRoomSize,
      parameters.createPassages
    )
    new CaveVisualizerScene(
      container,
      container.clientWidth,
      800,
      gen
    )
  }
}
