package app.streams

import app.domain.WindowEvent
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}
import zio.*
import zio.stream.ZStream

object Activator:
  import ZIO.*
  import app.common.BinStore.*

  enum HotKey(val code: Int):
    case Ctrl extends HotKey(NativeKeyEvent.VC_CONTROL)
    case Alt extends HotKey(NativeKeyEvent.VC_ALT)
    case Semicolon extends HotKey(NativeKeyEvent.VC_SEMICOLON)

  private val keysByCodes = HotKey.values.map(k => (k.code, k)).toMap[Int, HotKey]
  private val allPressed = HotKey.values.foldLeft(empty)(_ + _)

  trait GlobalKeyListener:
    def start: NativeKeyListener => Task[Unit]
    def stop: Task[Unit]

  object GlobalKeyListener:
    val empty: NativeKeyListener = new NativeKeyListener {}

  val globalKeyListener: TaskLayer[GlobalKeyListener] = ZLayer.fromZIO:
    for
      listener <- Ref.make(GlobalKeyListener.empty)
    yield new GlobalKeyListener:
      override def start: NativeKeyListener => Task[Unit] = l =>
        listener.set(l) *>
          attempt(GlobalScreen.registerNativeHook()) *>
          attempt(GlobalScreen.addNativeKeyListener(l))

      override def stop: Task[Unit] =
        listener.get.flatMap: l =>
          attempt(GlobalScreen.unregisterNativeHook()) *>
            attempt(GlobalScreen.removeNativeKeyListener(l))

  val toggler: ZStream[GlobalKeyListener, Throwable, WindowEvent] = ZStream.asyncScoped: cb =>
    for
      _ <- debug("start listen global key")
      pressedKeys <- Ref.make(empty)
      _ <- acquireRelease {
        for
          gkl <- service[GlobalKeyListener]
          _ <- gkl.start:
            new NativeKeyListener:
              override def nativeKeyPressed(nativeEvent: NativeKeyEvent): Unit =
                keysByCodes.get(nativeEvent.getKeyCode) match
                  case Some(code) => cb:
                    pressedKeys.updateAndGet(_ + code).map:
                      case s if s & allPressed => Chunk.single(WindowEvent.Toggle)
                      case _ => Chunk.empty
                  case _ => ()

              override def nativeKeyReleased(nativeEvent: NativeKeyEvent): Unit =
                keysByCodes.get(nativeEvent.getKeyCode) match
                  case Some(code) => cb:
                    pressedKeys.update(_ - code).map(_ => Chunk.empty)
                  case _ => ()
        yield gkl
      }(_.stop.orDie *> debug("stop listen global key"))
    yield ()
