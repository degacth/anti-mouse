package app.layer.window

import zio.*
import zio.ZIO.*

object Window:
  trait Service:
    def activate: UIO[Unit]
    def deactivate: UIO[Unit]

  def live: ZLayer[Frame.Service & Keys.Stream, Throwable, Service] = ZLayer.scoped:
    for
      frame <- service[Frame.Service]

    yield new Service:
      export frame.{activate, deactivate}
