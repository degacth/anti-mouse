package app.streams

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}
import zio.*
import zio.stream.ZStream

object Activator:
  import ZIO.*
  import app.common.BinStore.*

  enum HotKeys(c: Int):
    case Ctrl extends HotKeys(NativeKeyEvent.VC_CONTROL)
    case Alt extends HotKeys(NativeKeyEvent.VC_ALT)
    case Semicolon extends HotKeys(NativeKeyEvent.VC_SEMICOLON)

    def code: Int = c

  object HotKeys:
    val by: Int => Option[HotKeys] =
      case NativeKeyEvent.VC_CONTROL => Some(HotKeys.Ctrl)
      case NativeKeyEvent.VC_ALT => Some(HotKeys.Alt)
      case NativeKeyEvent.VC_SEMICOLON => Some(HotKeys.Semicolon)
      case _ => None

  enum Message:
    case Toggle
    case Activate
    case Deactivate

  trait GlobalKeyListener:
    def start: NativeKeyListener => Task[Unit]
    def stop: Task[Unit]

  object GlobalKeyListener:
    val empty: NativeKeyListener = new NativeKeyListener {}

  private val allPressed = empty + HotKeys.Ctrl + HotKeys.Alt + HotKeys.Semicolon

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

  val toggler: ZStream[GlobalKeyListener, Throwable, Message] = ZStream.asyncScoped: cb =>
    for
      _ <- debug("start listen global key")
      pressedKeys <- Ref.make(empty)
      _ <- acquireRelease {
        for
          gkl <- service[GlobalKeyListener]
          _ <- gkl.start:
            new NativeKeyListener:
              override def nativeKeyPressed(nativeEvent: NativeKeyEvent): Unit =
                HotKeys.by(nativeEvent.getKeyCode) match
                  case Some(code) => cb:
                    pressedKeys.updateAndGet(_ + code).map:
                      case s if s & allPressed => Chunk.single(Message.Toggle)
                      case _ => Chunk.empty
                  case _ => ()

              override def nativeKeyReleased(nativeEvent: NativeKeyEvent): Unit =
                HotKeys.by(nativeEvent.getKeyCode) match
                  case Some(code) => cb:
                    pressedKeys.update(_ - code).map(_ => Chunk.empty)
                  case _ => ()
        yield gkl
      }(_.stop.orDie *> debug("stop listen global key"))
    yield ()
