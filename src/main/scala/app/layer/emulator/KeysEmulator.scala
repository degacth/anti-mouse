package app.layer.emulator

import app.domain.CursorMove
import zio.ZIO.*
import zio.*
import zio.stream.*

import java.awt.event.KeyEvent

object KeysEmulator:

  trait Service:
    def emulate: ZStream[Any, Throwable, KeyEvent] => ZStream[Any, Throwable, CursorMove]

  val live: ZLayer[FastMove.Service, Throwable, Service] = ZLayer.scoped:
    for
      fastMove <- service[FastMove.Service]
    yield new Service:
      import app.matcher.KeyEventMatcher.*

      override def emulate: ZStream[Any, Throwable, KeyEvent] => ZStream[Any, Throwable, CursorMove] = _
        .changesWith: (v1, v2) =>
          v1.getID == v2.getID &&
            v1.getKeyCode == v2.getKeyCode &&
            v1.getModifiersEx == v2.getModifiersEx
        .collectZIO:
          case event @ Press |> AnyOne if fastMove has event => fastMove.absolute(event).map(CursorMove.Absolute(_, _))
