package app

import zio.*
import ZIO.*
import app.layer.activator.{Activator, GlobalActivator}
import app.layer.emulator.{Emulator, Modificator, Mouse}
import app.layer.window.{Frame, Keys, Window}
import zio.stream.UStream

object Main extends ZIOAppDefault:
  override def run: Task[Any] = application.catchAllCause(c => logError(c.prettyPrint))

  private def application: Task[Any] = {
    for
      window <- service[Window.Service]
      emulator <- service[Emulator.Service]
      modificator <- service[Modificator.Service]
      activator <- service[Activator.Service]

      _ <- activator.stream.foreach:
        case Activator.Status.Activated => window.activate
        case Activator.Status.Deactivated =>
          window.deactivate
            *> whenCaseZIO(modificator.state):
              case s if s ? Modificator.Mod.Shift => unit // right click
              case s if s ? Modificator.Mod.Alt => unit
              case _ => emulator.click
            *> emulator.restore
            *> modificator.restore
      .fork

      _ <- serviceWithZIO[Keys.Stream](_.foreach(emulator.move(_))).fork
      _ <- Console.readLine("PRESS ENTER ...")
    yield ()
  }
    .provide(
      Modificator.live,
      Mouse.live,
      Emulator.live,
      GlobalActivator.live,
      Activator.live,
      Keys.live,
      Frame.live,
      Window.live,
    )
