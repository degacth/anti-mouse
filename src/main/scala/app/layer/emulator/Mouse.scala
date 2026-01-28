package app.layer.emulator

import zio.*
import ZIO.*
import app.layer.window.Frame

import java.awt.{MouseInfo, Robot}

object Mouse:
  enum Direction(val x: Int, val y: Int):
    case Left extends Direction(-1, 0)
    case Down extends Direction(0, 1)
    case Right extends Direction(1, 0)
    case Up extends Direction(0, -1)

  import MouseInfo.{getPointerInfo => pointer}

  trait Service:
    def move(dir: Direction, press: Promise[Nothing, Unit]): UIO[Unit]

  def live: ZLayer[Frame.Service, Throwable, Service] = ZLayer.scoped:
    for
      robot <- attempt(Robot())
    yield new Service:
      private def moveRecursive(dir: Direction): UIO[Unit] =
        for
          (x, y) <- succeed(pointer.getLocation).map(i => (i.x, i.y))
          _ <- succeed(robot.mouseMove(x + dir.x, y + dir.y))
          _ <- moveRecursive(dir).delay(100.millis)
        yield ()

      override def move(dir: Direction, press: Promise[Nothing, Unit]): UIO[Unit] =
        for
          fiber <- moveRecursive(dir).fork
          _ <- press.await
          _ <- fiber.interrupt
        yield ()
