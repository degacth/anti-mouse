package app.layer.activator

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import zio.*
import zio.ZIO.*

object GlobalKeyListener:
  val empty: NativeKeyListener = new NativeKeyListener {}

  trait Service:
    def start: NativeKeyListener => Task[Unit]
    def stop: Task[Unit]

  val live: TaskLayer[Service] = ZLayer.fromZIO:
    for
      listener <- Ref.make(GlobalKeyListener.empty)
    yield new Service:
      override def start: NativeKeyListener => Task[Unit] = l =>
        listener.set(l) *>
          attempt(GlobalScreen.registerNativeHook()) *>
          attempt(GlobalScreen.addNativeKeyListener(l))

      override def stop: Task[Unit] =
        listener.get.flatMap: l =>
          attempt(GlobalScreen.unregisterNativeHook()) *>
            attempt(GlobalScreen.removeNativeKeyListener(l))
