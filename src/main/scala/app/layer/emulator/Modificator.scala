package app.layer.emulator

import app.layer.window.Keys
import zio.*
import ZIO.*
import app.common.BinStore

import java.awt.event.KeyEvent

object Modificator:
  type State = BinStore.State[Mod]
  
  trait Service:
    def hasShift: UIO[Boolean]
    def hasAlt: UIO[Boolean]
    def state: UIO[State]
    def restore: UIO[Unit]

  enum Mod:
    case Shift, Alt

  val live: ZLayer[Keys.Stream, Nothing, Service] = ZLayer.scoped:
    import BinStore.{State => _, *}

    for
      store <- Ref.make(BinStore.empty[Mod])
      _ <- serviceWithZIO[Keys.Stream]:
        _.foreach: e =>
          store
            .updateAndGet: s =>
              e.getID match
                case KeyEvent.KEY_PRESSED => s
                  .filtered(e.isAltDown || e.getKeyCode == KeyEvent.VK_ALT, _ + Mod.Alt)
                  .filtered(e.isShiftDown || e.getKeyCode == KeyEvent.VK_SHIFT, _ + Mod.Shift)
                case KeyEvent.KEY_RELEASED => s
                  .filtered(e.getKeyCode == KeyEvent.VK_ALT, _ - Mod.Alt)
                  .filtered(e.getKeyCode == KeyEvent.VK_SHIFT, _ - Mod.Shift)
        .fork

    yield new Service:
      override def hasShift: UIO[Boolean] = store.get.map(_ ? Mod.Shift)
      override def hasAlt: UIO[Boolean] = store.get.map(_ ? Mod.Alt)
      override def state: UIO[State] = store.get
      override def restore: UIO[Unit] = store.set(empty)
