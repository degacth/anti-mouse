package app.layer.modificator

import zio.*
import zio.ZIO.*

import java.awt.event.KeyEvent

object KeyModificator:
  import app.common.BinStore.*

  trait Service:
    def modify: KeyEvent => UIO[Unit]
    def changed: Queue[State[Mod]]

  val live: ZLayer[Any, Nothing, Service] = ZLayer.scoped:
    for
      state <- Ref.make(empty[Mod])
      stateChanges <- Queue.unbounded[State[Mod]]
    yield new Service:
      import KeyEvent.*

      override def modify: KeyEvent => UIO[Unit] = e =>
        val modifier = e.getID match
          case KeyEvent.KEY_PRESSED => addKey
          case KeyEvent.KEY_RELEASED => removeKey

        state
          .updateAndGet: s =>
            s
              .filtered(e.isControlDown || e.getKeyCode == VK_CONTROL, modifier(_, Mod.Ctrl))
              .filtered(e.isAltDown || e.getKeyCode == VK_ALT, modifier(_, Mod.Alt))
              .filtered(e.isShiftDown || e.getKeyCode == VK_SHIFT, modifier(_, Mod.Shift))
          .flatMap(stateChanges.offer(_) *> unit) *> state.get.flatMap(debug(_))

      private val addKey: (State[Mod], Mod) => State[Mod] = _ + _
      private val removeKey: (State[Mod], Mod) => State[Mod] = _ - _
      override def changed: Queue[State[Mod]] = stateChanges
