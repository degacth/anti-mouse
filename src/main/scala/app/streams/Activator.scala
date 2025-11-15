package app.streams

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}
import zio.*
import zio.stream.ZStream

object Activator:
  import ZIO.*

  enum Message:
    case Toggle
    case Activate
    case Deactivate

  trait GlobalKeyListener:
    def start: NativeKeyListener => Task[Unit]
    def stop: Task[Unit]

  object GlobalKeyListener:
    val empty: NativeKeyListener = new NativeKeyListener {}

  val pressToToggle: Seq[Int] = Seq(
    NativeKeyEvent.VC_CONTROL,
    NativeKeyEvent.VC_ALT,
    NativeKeyEvent.VC_SEMICOLON,
  )

  private val pressToToggleByCodes: Map[Int, Int] =
    pressToToggle
      .zipWithIndex
      .map { case (k, i) => (k, 1 << i) }
      .toMap

  private val allPressed: Int = pressToToggleByCodes.values.fold(0)(_ | _)

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
      pressedKeys <- Ref.make(0)
      _ <- acquireRelease {
        for
          gkl <- service[GlobalKeyListener]
          _ <- gkl.start:
            new NativeKeyListener:
              override def nativeKeyPressed(nativeEvent: NativeKeyEvent): Unit =
                pressToToggleByCodes.get(nativeEvent.getKeyCode) match
                  case Some(code) => cb:
                    pressedKeys
                      .updateAndGet(_ | code)
                      .map:
                        case v if v == allPressed => Chunk.single(Message.Toggle)
                        case _ => Chunk.empty
                  case _ => ()

              override def nativeKeyReleased(nativeEvent: NativeKeyEvent): Unit =
                pressToToggleByCodes.get(nativeEvent.getKeyCode) match
                  case Some(code) => cb:
                    pressedKeys
                      .update(v => (v | code) ^ code)
                      .map(_ => Chunk.empty)
                  case _ => ()
        yield gkl
      }(_.stop.orDie *> debug("stop listen global key"))
    yield ()
