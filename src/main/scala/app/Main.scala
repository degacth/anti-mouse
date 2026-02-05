package app

import zio.*
import ZIO.*
import app.layer.activator.{Activator, GlobalActivator}
import app.layer.emulator.{Emulator, Modificator, Mouse}
import app.layer.force.ForceMouse
import app.layer.window.{Frame, Keys}
import app.parameters.Parameters
import zio.stream.UStream

import java.awt.event.KeyEvent

object Main extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Parameters.configProvider

  override def run: Task[Any] = application.catchAllCause(c => logError(c.prettyPrint))

  private def application: Task[Any] = {
    for
      frame <- service[Frame.Service]
      emulator <- service[Emulator.Service]
      modificator <- service[Modificator.Service]
      activator <- service[Activator.Service]
      forceMouse <- service[ForceMouse.Service]

      _ <- activator.stream.foreach:
        case Activator.Status.Activated => frame.activate
        case Activator.Status.Deactivated =>
          frame.deactivate
            *> whenCaseZIO(modificator.state):
            case s if s ? Modificator.Mod.Shift => emulator.dblClick
            case s if s ? Modificator.Mod.Alt => unit
            case _ => emulator.click
          *> emulator.restore
            *> modificator.restore
      .fork

      globalKeyEvents <- service[Activator.GlobalKeyEvents]
      forceKeyEvents = globalKeyEvents
        .changesWith((e1, e2) => e1.getKeyCode == e2.getKeyCode && e1.getID == e2.getID)
        .tap:
          case e if forceMouse.forceActivator(e) => forceMouse.on
          case e if forceMouse.forceDeactivator(e) => forceMouse.off
          case _ => ZIO.unit

        .mapZIO(e => forceMouse.enabled.map((_, e)))
        .collect[KeyEvent]:
          case (true, e) => forceMouse.toForcedKey(e)

      windowKeys <- service[Keys.Stream]
      _ <- windowKeys
        .mapZIO(e => forceMouse.enabled.map((_, e)))
        .collect[KeyEvent]:
          case (false, e) => e
        .merge(forceKeyEvents)
        .tap(modificator.checkEvent)
        .foreach(k => modificator.state.flatMap(emulator.key(k, _)))
        .fork
      _ <- Console.readLine("PRESS ENTER TO EXIT ... \n")
    yield ()
  }
    .provide(
      Frame.live,
      Parameters.live,
      Modificator.live,
      Mouse.live,
      Emulator.live,
      GlobalActivator.live,
      Activator.live,
      Keys.live,
      ForceMouse.live,
    )
