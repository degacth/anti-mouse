package app

import zio.*
import ZIO.*
import app.layer.activator.{Activator, GlobalActivator}
import app.layer.window.{Frame, KeysStream, Window}

object Main extends ZIOAppDefault:
  override def run: Task[Any] = application.catchAllCause(debug(_))

  private def application: Task[Any] = {
    for
      window <- service[Window.Service]
      f <- serviceWithZIO[Activator.Service]: s =>
        s.stream
          .foreach:
            case Activator.Status.Activated => window.activate
            case Activator.Status.Deactivated => window.deactivate
          .fork
      _ <- window.keys.foreach: (k, p) =>
        (debug(s"pressed $k") *> p.await *> debug(s"released $k")).fork
      .fork

      _ <- Console.readLine("ENTER to stop")
      _ <- f.interrupt
    yield ()
  }
    .provide(
      Frame.live,
      KeysStream.live,
      Window.live,
      Activator.live,
      GlobalActivator.live,
    )
