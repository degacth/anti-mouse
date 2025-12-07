package app.layer

import zio.*

import java.awt.GraphicsEnvironment

object Screen:
  import ZIO.*

  trait Service:
    def screenPart(dim: (Int, Int), point: (Int, Int)): Task[(Int, Int)]

  trait Display:
    def size: Task[(Double, Double)]

  val display: TaskLayer[Display] = ZLayer.fromZIO:
    succeed:
      new Display:
        override def size: Task[(Double, Double)] = attempt {
          GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice.getDisplayMode
        }.map(display => (display.getWidth.toDouble, display.getHeight.toDouble))

  val live: RLayer[Display, Service] = ZLayer.scoped:
    for
      display <- service[Display]
    yield new Service:
        override def screenPart(dim: (Int, Int), point: (Int, Int)): Task[(Int, Int)] =
          for
            (dw, dh) <- display.size
            (sw, sh) = (dw / dim._1, dh / dim._2)
            (hw, hh) = (sw / 2, sh / 2)
          yield ((point._1 * sw + hw).toInt, (point._2 * sh + hh).toInt)
