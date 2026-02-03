package app.layer.emulator

import zio.*
import ZIO.*
import app.common.BinStore
import app.parameters.Parameters

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
    def randomMove: UIO[Unit]

    def isFasterMove: Modificator.State => Boolean = s =>
      s ? Modificator.Mod.Alt && s ? Modificator.Mod.Shift

    def isSlowerMove: Modificator.State => Boolean = s =>
      !(s ? Modificator.Mod.Alt) && s ? Modificator.Mod.Shift

    def isScroll: Modificator.State => Boolean = s =>
      s ? Modificator.Mod.Alt && !(s ? Modificator.Mod.Shift)

  def live: ZLayer[Modificator.Service & Parameters, Throwable, Service] = ZLayer.scoped:
    for
      parameters <- service[Parameters]
      robot <- attemptBlockingIO(Robot())
      modificator <- service[Modificator.Service]
      directions <- Ref.make(BinStore.empty[Direction])
    yield new Service:
      import BinStore.*

      private val steps = for
        x <- -1 to 1
        y <- -1 to 1
        if x != 0 && y != 0
      yield (x, y)

      private val scrollSpeed = 1
      private val rate = 1000 / parameters.cursor.rate
      private val toDirectionIndexes: (Direction, Direction, BinStore.State[Direction]) => Int =
        (opposite, forward, dirs) => dirs.present(opposite, 0, _ => -1) + dirs.present(forward, 0, _ => 1)

      private val pointerXY: UIO[(Int, Int)] = succeed(pointer.getLocation).map(i => (i.x, i.y))

      private def move: UIO[Unit] = {
        for
          dirs <- directions.get
          (x, y) <- pointerXY
          speed <- modificator.state.map:
            case mod if mod ? Modificator.Mod.Shift && mod ? Modificator.Mod.Alt => parameters.cursor.speed * 2
            case mod if mod ? Modificator.Mod.Shift => parameters.cursor.speed / 4
            case _ => parameters.cursor.speed
          _ <- succeed(robot.mouseMove(
            x + toDirectionIndexes(Direction.Left, Direction.Right, dirs) * speed,
            y + toDirectionIndexes(Direction.Up, Direction.Down, dirs) * speed,
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

      override def randomMove: UIO[Unit] =
        for
          (x, y) <- pointerXY
          (xDiff, yDiff) <- Random.nextIntBounded(steps.size).map(steps)
          _ <- succeed(robot.mouseMove(x + xDiff, y + yDiff))
        yield ()
