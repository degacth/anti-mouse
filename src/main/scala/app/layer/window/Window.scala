package app.layer.window

import zio.*
import zio.ZIO.*
import zio.stream.{UStream, ZStream}

import java.awt.event.KeyEvent

object Window:
  trait Service:
    def activate: UIO[Unit]
    def deactivate: UIO[Unit]
    def keys: ZStream[Any, Throwable, (Int, Promise[Nothing, Unit])]

  def live: ZLayer[Frame.Service & UStream[KeyEvent], Throwable, Service] = ZLayer.scoped:
    for
      frame <- service[Frame.Service]
      keyEvents <- service[UStream[KeyEvent]]
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
      override def keys: ZStream[Any, Throwable, (Int, Promise[Nothing, Unit])] = keyEventStream
