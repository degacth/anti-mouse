package app.layer

import zio.*

import java.awt.Robot
import java.awt.MouseInfo.getPointerInfo
import java.awt.event.InputEvent

object Emulator:
  import ZIO.*

  trait Service:
    def absolute(x: Int, y: Int): UIO[Unit]
    def moveStart: Move.Direction => UIO[Unit]
    def moveStop: Move.Direction => UIO[Unit]
    def click: UIO[Unit]

  val live: RLayer[Move.Service, Service] = ZLayer.scoped:
    for
      robot <- succeed(Robot())
      move <- service[Move.Service]
    yield new Service:
      override def absolute(x: Int, y: Int): UIO[Unit] = succeed(robot.mouseMove(x, y))
      override val moveStart: Move.Direction => UIO[Unit] = move.run(_, updateCursorPosition)
      override val moveStop: Move.Direction => UIO[Unit] = move.stop
      override def click: UIO[Unit] =
        succeed(robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)) *>
          succeed(robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK))

      private def updateCursorPosition(x: Int, y: Int): UIO[Unit] =
        succeed(getPointerInfo.getLocation).map(l => (l.x, l.y)).flatMap:
          case (lx, ly) => succeed(robot.mouseMove(lx + x, ly + y))
