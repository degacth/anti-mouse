package app.layer.emulator

import zio.*
import ZIO.*
import app.common.BinStore
import app.layer.window.Frame

import java.awt.event.KeyEvent

object Emulator:
  trait Service:
    def key: (KeyEvent, BinStore.State[Modificator.Mod]) => UIO[Unit]
    def click: UIO[Unit]
    def restore: UIO[Unit]
    def dblClick: UIO[Unit]

  private val moveKeys =
    import Mouse.Direction.*
    import java.awt.event.KeyEvent.*

    Map(
      VK_W -> Up,
      VK_K -> Up,
      VK_D -> Right,
      VK_L -> Right,
      VK_S -> Down,
      VK_J -> Down,
      VK_A -> Left,
      VK_H -> Left,
    )

  private type Deps = Mouse.Service & Frame.Service

  def live: ZLayer[Deps, Throwable, Service] = ZLayer.scoped:
    for
      mouse <- service[Mouse.Service]
    yield new Service:
      private val isInMoveKeysEvent: (KeyEvent, Int) => Boolean = (e, id) =>
        (moveKeys contains e.getKeyCode) && e.getID == id

      private val isPressed: KeyEvent => Boolean = e => isInMoveKeysEvent(e, KeyEvent.KEY_PRESSED)
      private val isReleased: KeyEvent => Boolean = e => isInMoveKeysEvent(e, KeyEvent.KEY_RELEASED)
      private val isVerticalKey: KeyEvent => Boolean = e =>
        import Mouse.Direction.{Up, Down}
        moveKeys.get(e.getKeyCode).exists(v => v == Up || v == Down)

      override val key: (KeyEvent, BinStore.State[Modificator.Mod]) => UIO[Unit] =
        case (e, mod) if isVerticalKey(e) && mod ? Modificator.Mod.Alt => mouse.scroll(moveKeys(e.getKeyCode))
        case (e, _) if isPressed(e) => mouse.startMove(moveKeys(e.getKeyCode))
        case (e, _) if isReleased(e) => mouse.stopMove(moveKeys(e.getKeyCode))
        case _ => unit

      override def click: UIO[Unit] = mouse.click
      override def restore: UIO[Unit] = mouse.restore
      override def dblClick: UIO[Unit] = mouse.click *> mouse.click.delay(100.millis)
