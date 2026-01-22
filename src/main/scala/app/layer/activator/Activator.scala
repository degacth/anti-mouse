package app.layer.activator

import app.layer.activator.GlobalKeyListener
import com.github.kwhat.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}
import zio.*
import zio.stream.{Stream, ZStream}

import java.util.concurrent.TimeUnit

object Activator:
  import ZIO.*
  import app.layer.activator.Activator.Status.*

  val actionTimeout: Int = 300
  val actionKey: Int = NativeKeyEvent.VC_CONTROL

  enum Status:
    case Activated, Deactivated

  trait Service:
    def stream: Stream[Throwable, Status]

  def live: RLayer[GlobalKeyListener.Service, Activator.Service] = ZLayer.scoped:
    for
      gkl <- service[GlobalKeyListener.Service]
      keys = ZStream.asyncScoped[Any, Throwable, Status]: cb =>
        for
          _ <- debug("start listen global key")
          _ <- acquireRelease {
            for
              _ <- gkl.start:
                new NativeKeyListener:
                  override def nativeKeyPressed(e: NativeKeyEvent): Unit =
                    e.getKeyCode match
                      case code if code == actionKey => cb:
                        succeed(Chunk.single(Activated))
                      case _ => ()

                  override def nativeKeyReleased(e: NativeKeyEvent): Unit = e.getKeyCode match
                    case code if code == actionKey => cb:
                      succeed(Chunk.single(Deactivated))
                    case _ => ()
            yield gkl
          }(_.stop.orDie *> debug("stop listen global key"))
        yield ()
      events <- keys
        .partition(_ == Deactivated)
        .map((released, pressed) => released.merge(
          pressed
            .mapZIO(_ => Clock.currentTime(TimeUnit.MILLISECONDS))
            .zipWithPrevious
            .collect:
              case (Some(prev), curr) if curr - prev < 600 => Activated
        ))
    yield new Service:
      override val stream: Stream[Throwable, Status] = events.changes
