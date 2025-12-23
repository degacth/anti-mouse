package app.layer

import zio.ZLayer
import zio.*
import ZIO.*
import app.domain.{CursorAction, Direction}
import app.layer.modificator.KeyModificator
import app.layer.window.Window2
import app.layer.window.WindowAction.FocusLost
import zio.stream.ZStream

import java.awt.Robot
import java.awt.MouseInfo.getPointerInfo
import java.awt.event.InputEvent

object Cursor:
  trait Service:
    def move: ZStream[Any, Throwable, CursorAction] => ZStream[Any, Throwable, Any]

  private type Deps = KeyModificator.Service & Window2.Service

  val live: ZLayer[Deps, Throwable, Service] = ZLayer.scoped:
    import app.common.BinStore.*
    import app.layer.modificator.Mod

    for
      robot <- attempt(Robot())
      direction <- Ref.make(empty[Direction])
      baseSpeed = 7
      speed <- Ref.make(baseSpeed)
      _ <- serviceWithZIO[KeyModificator.Service]: s =>
        ZStream.fromQueue(s.changed)
          .foreach:
            case s if s ? Mod.Ctrl && s ? Mod.Shift => speed.set((baseSpeed * .8).toInt)
            case s if s ? Mod.Ctrl => speed.set((baseSpeed * .5).toInt)
            case s if s ? Mod.Shift => speed.set(baseSpeed * 2)
            case _ => speed.set(baseSpeed)
          .fork

      _ <- serviceWithZIO[Window2.Service]:
        _.windowActions
          .collectZIO:
            case FocusLost => direction.set(empty)
          .runDrain
          .fork
    yield new Service:
      import app.domain.CursorAction.*

      override def move: ZStream[Any, Throwable, CursorAction] => ZStream[Any, Throwable, Any] = _
        .mapZIO:
          case Absolute(x, y) => attempt(robot.mouseMove(x, y))
          case StartMove(dir) => direction
            .getAndUpdate(_ + dir)
            .flatMap:
              case EmptyState => directionMove.fork *> unit
              case _ => unit
          case StopMove(dir) => direction.update(_ - dir)
          case Click => attempt(robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)) *>
            attempt(robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK))

      private def directionMove: Task[Unit] = direction.get.flatMap:
        case EmptyState => unit
        case dir => for
          location <- attempt(getPointerInfo.getLocation)
          (x, y) = (
            dir.present(Direction.Left, 0, _.diff) + dir.present(Direction.Right, 0, _.diff),
            dir.present(Direction.Up, 0, _.diff) + dir.present(Direction.Down, 0, _.diff),
          )
          s <- speed.get
          freq = 1000 / 30
          _ <- attempt(robot.mouseMove((x * s) + location.x, (y * s) + location.y))
          _ <- directionMove.delay(freq.millis)
        yield ()
