package app.layer.activator

import zio.*
import ZIO.*
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}
import zio.stream.ZStream

object GlobalActivator:
  import Activator.Status.*

  val live: ZLayer[Any, Throwable, Activator.Activations] = ZLayer.scoped:
    succeed:
      ZStream.asyncScoped: cb =>
        debug("start listen global keys")
          *> acquireRelease {
          val listener = new NativeKeyListener:
            private val emitter: Activator.Status => NativeKeyEvent => Unit = status =>
              case e if e.getKeyCode == Activator.actionKey => cb(succeed(Chunk.single(status)))
              case _ => ()

            override def nativeKeyPressed(nativeEvent: NativeKeyEvent): Unit = emitter(Activated)(nativeEvent)
            override def nativeKeyReleased(nativeEvent: NativeKeyEvent): Unit = emitter(Deactivated)(nativeEvent)

          attempt(GlobalScreen.registerNativeHook())
            *> attempt(GlobalScreen.addNativeKeyListener(listener))
            *> succeed(listener)
        }(l =>
          succeed(GlobalScreen.removeNativeKeyListener(l))
            *> succeed(GlobalScreen.unregisterNativeHook())
            *> debug("stop listen global keys")
        )
