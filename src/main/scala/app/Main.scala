package app

import app.streams.Activator
import zio.*

object Main extends ZIOAppDefault:
  private val application =
    for
      _ <- Activator.stream.foreach(n => ZIO.debug(n))
    yield ()

  override def run: Task[ExitCode] = ZIO.scoped:
    for
      fiber <- application
        .provideLayer(Activator.live)
        .fork
      _ <- Console.readLine
      _ <- fiber.interrupt
    yield ExitCode.success
