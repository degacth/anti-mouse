package app

import app.layer.{Emulator, Screen}
import app.streams.{Activator, Mouse, Window}
import zio.*
import zio.stream.ZStream

object Main extends ZIOAppDefault:
  private val application = {
    (Activator.toggler >>> Window.activator).merge:
      Window.keyboardStream >>> Mouse.keysToMouse
  }
    .catchAll(e => ZStream.fromZIO(ZIO.debug(e.getMessage)))
    .tap(ZIO.debug(_))
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
        )
        .catchAll(ZIO.debug(_))
        .fork
      _ <- Console.readLine
      _ <- fiber.interrupt
    yield ExitCode.success
