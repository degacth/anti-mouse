package app.layer.force

import app.layer.force.transformer.ForsedKey
import app.layer.window.Frame
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import zio.*

import java.awt.event.KeyEvent

object ForceMouse:
  trait Service:
    def on: UIO[Unit]
    def off: UIO[Unit]
    def enabled: UIO[Boolean]
    def toForcedKey: NativeKeyEvent => KeyEvent = ForsedKey.apply

    def forceActivator: NativeKeyEvent => Boolean = e =>
      e.getKeyCode == NativeKeyEvent.VC_SPACE && e.getID == NativeKeyEvent.NATIVE_KEY_PRESSED

    def forceDeactivator: NativeKeyEvent => Boolean = e =>
      e.getKeyCode == NativeKeyEvent.VC_SPACE && e.getID == NativeKeyEvent.NATIVE_KEY_RELEASED

  val live: ZLayer[Frame.Service, Nothing, Service] = ZLayer.fromZIO:
    for
      state <- Ref.make(false)
      frame <- ZIO.service[Frame.Service]
    yield new Service:
      export state.{get => enabled}
      override def on: UIO[Unit] = state.set(true) *> frame.highlight
      override def off: UIO[Unit] = state.set(false) *> frame.offHighlight
      override def enabled: UIO[Boolean] = state.get
