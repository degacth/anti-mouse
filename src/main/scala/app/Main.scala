package app

import zio.*
import ZIO.*
import app.layer.activator.{Activator, GlobalKeyListener}
import app.layer.window.Window

object Main extends ZIOAppDefault:
  override def run: Task[Any] = application.catchAllCause(debug(_))

  private def application: Task[Any] =
    (for
      window <- service[Window.Service]
      f <- serviceWithZIO[Activator.Service]: s =>
        s.stream
          .foreach:
            case Activator.Status.Activated => window.activate
            case Activator.Status.Deactivated => window.deactivate
          .fork
      _ <- window.keyPress.debug.runDrain.fork
      _ <- window.keyRelease.debug.runDrain.fork

      _ <- Console.readLine("ENTER to stop")
      _ <- f.interrupt
    yield ()
      ).provide(
      Window.live,
      Activator.live,
      GlobalKeyListener.live,
    )
