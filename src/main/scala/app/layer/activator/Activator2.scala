package app.layer.activator

import app.layer.activator.GlobalKeyListener
import com.github.kwhat.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}
import zio.*
import zio.stream.{Stream, ZStream}

object Activator2:

  import ZIO.*

  case object Toggle

  enum HotKey(val code: Int):
    case Ctrl extends HotKey(NativeKeyEvent.VC_CONTROL)
    case Alt extends HotKey(NativeKeyEvent.VC_ALT)
    case Semicolon extends HotKey(NativeKeyEvent.VC_SEMICOLON)

  trait Service:
    def toggler: Stream[Throwable, Toggle.type]

  def live: RLayer[GlobalKeyListener.Service, Service] = ZLayer.fromZIO:
    import app.common.BinStore.*

    val keysByCodes = HotKey.values.map(k => (k.code, k)).toMap[Int, HotKey]
    val allPressed = HotKey.values.foldLeft(empty[HotKey])(_ + _)

    for
      gkl <- service[GlobalKeyListener.Service]
      togglerStream = ZStream.asyncScoped[Any, Throwable, Toggle.type]: cb =>
        for
          _ <- debug("start listen global key")
          pressedKeys <- Ref.make(empty[HotKey])
          _ <- acquireRelease {
            for
              _ <- gkl.start:
                new NativeKeyListener:
                  override def nativeKeyPressed(e: NativeKeyEvent): Unit = e.getKeyCode match
                    case code if keysByCodes.contains(code) => cb:
                      pressedKeys.updateAndGet(_ + keysByCodes(code)).map:
                        case s if s & allPressed => Chunk.single(Toggle)
                        case _ => Chunk.empty
                    case _ => ()

                  override def nativeKeyReleased(e: NativeKeyEvent): Unit = e.getKeyCode match
                    case code if keysByCodes.contains(code) => cb:
                      pressedKeys.update(_ - keysByCodes(code)).map(_ => Chunk.empty)
                    case _ => ()
            yield gkl
          }(_.stop.orDie *> debug("stop listen global key"))
        yield ()
    yield new Service:
      override val toggler: Stream[Throwable, Toggle.type] = togglerStream
