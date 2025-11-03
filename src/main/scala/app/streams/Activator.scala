package app.streams

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}
import zio.*
import zio.stream.ZStream

object Activator:
  import ZIO.*

  case object Toggle

  trait GlobalKeyListener:
    def start: NativeKeyListener => UIO[Unit]
    def stop: UIO[Unit]

  object GlobalKeyListener:
    val empty: NativeKeyListener = new NativeKeyListener {}

  val pressToToggle: Seq[Int] = Seq(NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_ALT, NativeKeyEvent.VC_SEMICOLON)

  private val pressToToggleByCodes: Map[Int, Int] = pressToToggle.zipWithIndex
    .map { case (k, i) => (k, 1 << i) }
    .toMap

  private val allPressed: Int = pressToToggleByCodes.values.fold(0)(_ | _)

  val live: ULayer[GlobalKeyListener] = ZLayer.succeed:
    val listenerRef = Ref.make(GlobalKeyListener.empty)
    new GlobalKeyListener:
      override def start: NativeKeyListener => UIO[Unit] = l => listenerRef.flatMap(_.set(l)) *>
        succeed(GlobalScreen.registerNativeHook()) *>
        succeed(GlobalScreen.addNativeKeyListener(l))

      override def stop: UIO[Unit] =
        for
          listener <- listenerRef.flatMap(_.get)
          _ <- succeed(GlobalScreen.unregisterNativeHook()) *> succeed(GlobalScreen.removeNativeKeyListener(listener))
        yield ()

  val stream: ZStream[GlobalKeyListener, Nothing, Toggle.type] = ZStream.asyncScoped: cb =>
    for
      _ <- debug("start listen global key")
      pressedKeys <- Ref.make(0)
      _ <- acquireRelease {
        for
          gkl <- service[GlobalKeyListener]
          _ <- gkl.start:
            new NativeKeyListener:
              override def nativeKeyPressed(nativeEvent: NativeKeyEvent): Unit =
                pressToToggleByCodes.get(nativeEvent.getKeyCode) match
                  case Some(code) => cb {
                    pressedKeys
                      .updateAndGet(_ | code)
                      .map:
                        case v if v == allPressed => Chunk.single(Toggle)
                        case _ => Chunk.empty
                  }
                  case _ => ()

              override def nativeKeyReleased(nativeEvent: NativeKeyEvent): Unit =
                pressToToggleByCodes.get(nativeEvent.getKeyCode) match
                  case Some(code) => cb {
                    pressedKeys.update(_ ^ code).map(_ => Chunk.empty)
                  }
                  case _ => ()
        yield gkl
      }(_.stop *> debug("stop listen global key"))
    yield ()
