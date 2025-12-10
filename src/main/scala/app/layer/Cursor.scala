package app.layer

import zio.ZLayer
import zio.*
import ZIO.*
import app.domain.{CursorAction, Direction}
import zio.stream.ZStream

import java.awt.Robot
import java.awt.MouseInfo.getPointerInfo

object Cursor:
  trait Service:
    def move: ZStream[Any, Throwable, CursorAction] => ZStream[Any, Throwable, Any]

  val live: ZLayer[Any, Throwable, Service] = ZLayer.scoped:
    import app.common.BinStore.*
    for
      robot <- attempt(Robot())
      direction <- Ref.make(empty[Direction])
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

      private def directionMove: Task[Unit] = direction.get.flatMap:
          case EmptyState => unit
          case dir => for
            location <- attempt(getPointerInfo.getLocation)
            (x, y) = (
              dir.present(Direction.Left, 0, _.diff) + dir.present(Direction.Right, 0, _.diff),
              dir.present(Direction.Up, 0, _.diff) + dir.present(Direction.Down, 0, _.diff),
              )
            speed = 5
            freq = 1000 / 30
            _ <- attempt(robot.mouseMove((x * speed) + location.x, (y * speed) + location.y))
            _ <- directionMove.delay(freq.millis)
          yield ()
