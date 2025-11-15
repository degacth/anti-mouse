package app

import app.layer.{Emulator, Modificator, Move, Screen}
import app.streams.{Activator, Mouse, Window}
import zio.*
import ZIO.*
import zio.stream.{ZPipeline, ZStream}

object Main extends ZIOAppDefault:
  private val application =
    for
      window <- service[Window.Service]
      _ <- {
        (Activator.toggler.merge(window.commands) >>> Window.activator).merge:
          Window.keyboardStream
            .groupByKey(Mouse.streamKeyResolver): (k, s) =>
              Mouse.handlers
                .get(k)
                .fold(s >>> ZPipeline.mapZIO(m => ZIO.debug(s"unhandled key $k with message $m")))
                .apply(_(s))
      }
        .catchAllCause(c => ZStream.fromZIO(ZIO.debug(c.prettyPrint)))
        .runDrain
    yield ()

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
          Modificator.live,
          ZLayer.succeed(Move.speed(3)),
          ZLayer.succeed(Move.rate(16)),
        )
        .catchAll(ZIO.debug(_))
        .fork
      _ <- Console.readLine
      _ <- fiber.interrupt
    yield ExitCode.success
