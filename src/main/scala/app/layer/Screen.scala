package app.layer

import zio.*

import java.awt.GraphicsEnvironment

object Screen:
  import ZIO.*

  trait Service:
    def screenPart(dim: (Int, Int), point: (Int, Int)): RIO[Display, (Int, Int)]

  trait Display:
    def size: Task[(Double, Double)]

  val display: TaskLayer[Display] = ZLayer.fromZIO:
    succeed:
      new Display:
        override def size: Task[(Double, Double)] = attempt {
          GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice.getDisplayMode
        }.map(display => (display.getWidth.toDouble, display.getHeight.toDouble))

  val live: RLayer[Display, Service] = ZLayer.scoped:
    ZIO.succeed:
      new Service:
        override def screenPart(dim: (Int, Int), point: (Int, Int)): RIO[Display, (Int, Int)] =
          for
            (dw, dh) <- serviceWithZIO[Display](_.size)
            (sw, sh) = (dw / dim._1, dh / dim._2)
            (hw, hh) = (sw / 2, sh / 2)
          yield ((point._1 * sw + hw).toInt, (point._2 * sh + hh).toInt)
