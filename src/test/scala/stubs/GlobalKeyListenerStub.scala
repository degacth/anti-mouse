package stubs

import app.layer.activator.GlobalKeyListener
import com.github.kwhat.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}
import zio.*
import zio.ZIO.*

class GlobalKeyListenerStub extends GlobalKeyListener.Service:
  import GlobalKeyListenerStub.StubbedKeyEvent

  private var listener: Option[NativeKeyListener] = None

  override val start: NativeKeyListener => UIO[Unit] = l => succeed(this.listener = Option(l))
  override val stop: UIO[Unit] = succeed(())

  def pressed(codes: Int*): UIO[Unit] = foreachDiscard(codes): code =>
    succeed(listener.foreach(_.nativeKeyPressed(StubbedKeyEvent(code))))

  def released(codes: Int*): UIO[Unit] = foreachDiscard(codes): code =>
    succeed(listener.foreach(_.nativeKeyReleased(StubbedKeyEvent(code))))

object GlobalKeyListenerStub:
  val live: ULayer[GlobalKeyListener.Service & GlobalKeyListenerStub] = ZLayer.succeed(GlobalKeyListenerStub())

  case class StubbedKeyEvent(code: Int) extends NativeKeyEvent(0, 0, 0, code, Char.MinValue)
