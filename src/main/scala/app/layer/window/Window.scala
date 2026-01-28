package app.layer.window

import zio.*
import zio.ZIO.*
import zio.stream.ZStream

import java.awt.event.KeyEvent

object Window:
  private type KeyPresses = ZStream[Any, Throwable, (Int, Promise[Nothing, Unit])]

  trait Service:
    def activate: UIO[Unit]
    def deactivate: UIO[Unit]
    def keys: KeyPresses

  def live: ZLayer[Frame.Service & Keys.Stream, Throwable, Service] = ZLayer.scoped:
    for
      frame <- service[Frame.Service]
      keyEvents <- service[Keys.Stream]
      keyEventStream = keyEvents.groupByKey(_.getKeyCode):
        (k, s) =>
          s.mapAccumZIO(Option.empty[Promise[Nothing, Unit]]) {
              case (Some(p), e) if e.getID == KeyEvent.KEY_RELEASED => p.succeed(()).map(_ => (None, None))
              case (None, e) if e.getID == KeyEvent.KEY_PRESSED =>
                Promise.make[Nothing, Unit].map(p => (Some(p), Some((k, p))))
              case (s, _) => succeed((s, None))
            }
            .collect:
              case Some((k, p)) => (k, p)

    yield new Service:
      export frame.{activate, deactivate}
      override def keys: KeyPresses = keyEventStream
