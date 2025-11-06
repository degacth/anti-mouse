package app.layer

import zio.*

import java.awt.Robot

object Emulator:
  import ZIO.*

  trait Service:
    def absolute(x: Int, y: Int): UIO[Unit]

  val live: TaskLayer[Service] = ZLayer.fromZIO:
    for
      robot <- succeed(Robot())
    yield new Service:
      override def absolute(x: Int, y: Int): UIO[Unit] = succeed(robot.mouseMove(x, y))
