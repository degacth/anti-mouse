package stubs

import app.streams.Activator
import app.streams.Activator.HotKeys
import com.github.kwhat.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}
import zio.*

class GlobalKeyListenerStub extends Activator.GlobalKeyListener:
  import zio.ZIO.*
  import GlobalKeyListenerStub.*

  private var listener: Option[NativeKeyListener] = None

  override def start: NativeKeyListener => UIO[Unit] = l => succeed(this.listener = Option(l))
  override def stop: UIO[Unit] = succeed(())

  def pressed(keys: HotKeys*): UIO[Unit] = foreachDiscard(keys): key =>
    succeed(listener.foreach(_.nativeKeyPressed(KeyEvent(key.code))))

  def released(keys: HotKeys*): UIO[Unit] = foreachDiscard(keys): key =>
    succeed(listener.foreach(_.nativeKeyReleased(KeyEvent(key.code))))

object GlobalKeyListenerStub:
  val live: ULayer[Activator.GlobalKeyListener & GlobalKeyListenerStub] = ZLayer.succeed(GlobalKeyListenerStub())

  class KeyEvent(code: Int) extends NativeKeyEvent(0, 0, 0, code, Char.MinValue, 0)
