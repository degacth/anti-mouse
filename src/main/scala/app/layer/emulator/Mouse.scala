package app.layer.emulator

import zio.*
import ZIO.*
import app.common.BinStore
import app.layer.window.Frame

import java.awt.event.InputEvent
import java.awt.{MouseInfo, Robot}

object Mouse:
  enum Direction:
    case Up, Left, Down, Right

  import MouseInfo.{getPointerInfo => pointer}

  trait Service:
    def startMove(dir: Direction): UIO[Unit]
    def stopMove(dir: Direction): UIO[Unit]
    def scroll(dir: Direction): UIO[Unit]
    def click: UIO[Unit]
    def restore: UIO[Unit]

  def live: ZLayer[Modificator.Service, Throwable, Service] = ZLayer.scoped:
    for
      robot <- attemptBlockingIO(Robot())
      modificator <- service[Modificator.Service]
      directions <- Ref.make(BinStore.empty[Direction])
    yield new Service:
      import BinStore.*

      private val speed = 12
      private val scrollSpeed = 1
      private val rate = 1000 / 40
      private val toDirectionIndexes: (Direction, Direction, BinStore.State[Direction]) => Int =
        (opposite, forward, dirs) => dirs.present(opposite, 0, _ => -1) + dirs.present(forward, 0, _ => 1)

      private val pointerXY: UIO[(Int, Int)] = succeed(pointer.getLocation).map(i => (i.x, i.y))

      private def move: UIO[Unit] = {
        for
          dirs <- directions.get
          (x, y) <- pointerXY
          spd <- modificator.state.map: modState =>
            speed / modState.present(Modificator.Mod.Shift, 1, _ => 4)
          _ <- succeed(robot.mouseMove(
            x + toDirectionIndexes(Direction.Left, Direction.Right, dirs) * spd,
            y + toDirectionIndexes(Direction.Up, Direction.Down, dirs) * spd,
          ))
          _ <- move.delay(rate.millis)
        yield ()
      }
        .whenZIO(directions.get.map(_ != EmptyState)).unit

      override def startMove(dir: Direction): UIO[Unit] =
        directions.getAndUpdate(_ + dir).flatMap:
          case BinStore.EmptyState => move.fork *> unit
          case _ => unit

      override def stopMove(dir: Direction): UIO[Unit] = directions.update(_ - dir)
      override def click: UIO[Unit] =
        for
          _ <- succeed(robot.mousePress(InputEvent.BUTTON1_DOWN_MASK))
          _ <- succeed(robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)).delay(30.millis)
        yield ()

      override def restore: UIO[Unit] = directions.set(empty)
      override def scroll(dir: Direction): UIO[Unit] =
        val amt = dir match
          case Direction.Up => -1
          case Direction.Down => 1
          case _ => 0

        succeed(robot.mouseWheel(amt * scrollSpeed))
