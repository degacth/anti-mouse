package app.layer

import zio.ZLayer
import zio.*
import ZIO.*
import app.domain.CursorMove
import zio.stream.ZStream

import java.awt.Robot

object Cursor:
  trait Service:
    def move: ZStream[Any, Throwable, CursorMove] => ZStream[Any, Throwable, Any]

  val live: ZLayer[Any, Throwable, Service] = ZLayer.scoped:
    for
      robot <- attempt(Robot())
    yield new Service:
      override def move: ZStream[Any, Throwable, CursorMove] => ZStream[Any, Throwable, Any] = _
        .mapZIO:
          case CursorMove.Absolute(x, y) => attempt(robot.mouseMove(x, y))
