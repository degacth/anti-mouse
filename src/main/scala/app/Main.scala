package app

import app.streams.{Activator, Window}
import zio.*

object Main extends ZIOAppDefault:
  private val application =
    for
      _ <- (Activator.toggler >>> Window.activator).runDrain.fork
      _ <- Window.keyboardStream.runDrain
    yield ()

  override def run: Task[ExitCode] = ZIO.scoped:
    for
      fiber <- application
        .provide(
          Activator.globalKeyListener,
          Window.frame,
        )
        .fork
      _ <- Console.readLine
      _ <- fiber.interrupt
    yield ExitCode.success
