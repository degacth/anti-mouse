package app

import app.layer.{Emulator, Modification, Move, Screen}
import app.streams.{Activator, Mouse, Window}
import zio.*
import zio.stream.{ZPipeline, ZStream}

object Main extends ZIOAppDefault:
  private val application = {
    (Activator.toggler >>> Window.activator).merge:
      Window.keyboardStream
        .groupByKey(Mouse.streamKeyResolver): (k, s) =>
          Mouse.handlers
            .get(k)
            .fold(s >>> ZPipeline.mapZIO(m => ZIO.debug(s"unhandled key $k with message $m")))
            .apply(_(s))
  }
    .catchAllCause(c => ZStream.fromZIO(ZIO.debug(c.prettyPrint)))
    .runDrain

  override def run: Task[ExitCode] = ZIO.scoped:
    for
      fiber <- application
        .provide(
          Activator.globalKeyListener,
          Window.frame,
          Emulator.live,
          Screen.live,
          Screen.display,
          Move.live,
          Modification.live,
          ZLayer.succeed(Move.speed(3)),
          ZLayer.succeed(Move.rate(16)),
        )
        .catchAll(ZIO.debug(_))
        .fork
      _ <- Console.readLine
      _ <- fiber.interrupt
    yield ExitCode.success
