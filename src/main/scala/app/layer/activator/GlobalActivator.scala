package app.layer.activator

import zio.*
import ZIO.*
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}
import zio.stream.ZStream

object GlobalActivator:
  import Activator.Status.*
  import Activator.GlobalKeyEvents
  import Activator.Activations

  private val splitted: ZLayer[Any, Throwable, (Activations, GlobalKeyEvents)] =
    ZLayer.scoped:
      for
        globalEvents <- ZStream.asyncScoped[Any, Throwable, NativeKeyEvent]: cb =>
          debug("start listen global keys")
            *> acquireRelease {
            val listener = new NativeKeyListener:
              override def nativeKeyPressed(e: NativeKeyEvent): Unit = cb(succeed(Chunk.single(e)))
              override def nativeKeyReleased(e: NativeKeyEvent): Unit = cb(succeed(Chunk.single(e)))

            attemptBlockingIO(GlobalScreen.registerNativeHook())
              *> attempt(GlobalScreen.addNativeKeyListener(listener))
              *> succeed(listener)
          }(l =>
            succeed(GlobalScreen.removeNativeKeyListener(l))
              *> succeed(GlobalScreen.unregisterNativeHook())
              *> debug("stop listen global keys")
          )
        .broadcast(2, 5)
      yield (
        globalEvents.head.collect:
          case e if e.getID == NativeKeyEvent.NATIVE_KEY_PRESSED && e.getKeyCode == Activator.actionKey => Activated
          case e if e.getID == NativeKeyEvent.NATIVE_KEY_RELEASED && e.getKeyCode == Activator.actionKey => Deactivated
        ,
        globalEvents(1)
      )

  val live: ZLayer[Any, Throwable, Activations & GlobalKeyEvents] = splitted
    .flatMap(env => ZLayer.succeed(env.get._2)
      .++[Throwable, Any, GlobalKeyEvents, Activations](ZLayer.succeed(env.get._1))
    )
