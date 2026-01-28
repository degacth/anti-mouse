package app

import zio.*
import ZIO.*
import app.layer.activator.{Activator, GlobalActivator}
import app.layer.emulator.{Emulator, Mouse}
import app.layer.window.{Frame, Keys, Window}
import zio.stream.UStream

object Main extends ZIOAppDefault:
  override def run: Task[Any] = application.catchAllCause(debug(_))

  private def application: Task[Any] = {
    for
      window <- service[Window.Service]
      activator <- service[Activator.Service]
      _ <- activator.stream.foreach:
        case Activator.Status.Activated => window.activate
        case Activator.Status.Deactivated => window.deactivate
      .fork

      emulator <- service[Emulator.Service]
      _ <- window.keys.foreach:
        case (k, p) => emulator.move(k, p).fork
      .fork
      _ <- Console.readLine("PRESS ENTER ...")
    yield ()
  }
    .provide(
      Mouse.live,
      Emulator.live,
      GlobalActivator.live,
      Activator.live,
      Keys.live,
      Frame.live,
      Window.live
    )
