package app.layer.emulator

import app.domain.CursorAction
import app.layer.modificator.KeyModificator
import zio.ZIO.*
import zio.*
import zio.stream.*

import java.awt.event.KeyEvent

object KeysEmulator:

  trait Service:
    def emulate: ZStream[Any, Throwable, KeyEvent] => ZStream[Any, Throwable, CursorAction]

  type Deps = FastMove.Service & DirectionMove.Service & KeyModificator.Service

  val live: ZLayer[Deps, Throwable, Service] = ZLayer.scoped:
    for
      fastMove <- service[FastMove.Service]
      directionMove <- service[DirectionMove.Service]
      modificator <- service[KeyModificator.Service]
    yield new Service:
      override def emulate: ZStream[Any, Throwable, KeyEvent] => ZStream[Any, Throwable, CursorAction] = _
        .changesWith: (v1, v2) =>
          v1.getID == v2.getID &&
            v1.getKeyCode == v2.getKeyCode &&
            v1.getModifiersEx == v2.getModifiersEx
        .tap(e => modificator.modify(e))
        .collectZIO:
          case event if fastMove has event => fastMove.absolute(event)
          case event if directionMove has event => (event.getID, event.getKeyCode) match
            case (KeyEvent.KEY_PRESSED, code) => succeed(directionMove.start(code))
            case (KeyEvent.KEY_RELEASED, code) => succeed(directionMove.stop(code))
            case _ => fail(new IllegalStateException("Wrong logic to check direction move"))
