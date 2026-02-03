package app

import zio.*
import ZIO.*
import app.layer.activator.{Activator, GlobalActivator}
import app.layer.emulator.{Emulator, Modificator, Mouse}
import app.layer.window.{Frame, Keys}
import app.parameters.Parameters
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
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

      forceMouse <- Ref.make(false)
      globalKeyEvents <- service[Activator.GlobalKeyEvents]
      forceKeyEvents = globalKeyEvents
        .changesWith((e1, e2) => e1.getKeyCode == e2.getKeyCode && e1.getID == e2.getID)
        .tap:
          case e if e.getKeyCode == NativeKeyEvent.VC_SPACE && e.getID == NativeKeyEvent.NATIVE_KEY_PRESSED =>
            forceMouse.set(true) *> frame.highlight
          case e if e.getKeyCode == NativeKeyEvent.VC_SPACE && e.getID == NativeKeyEvent.NATIVE_KEY_RELEASED =>
            forceMouse.set(false) *> frame.offHighlight
          case _ => ZIO.unit

        .mapZIO(e => forceMouse.get.map((_, e)))
        .collect[KeyEvent]:
          case (true, e) => Activator.GlobalKeyEvent(e)

      windowKeys <- service[Keys.Stream]
      _ <- windowKeys
        .mapZIO(e => forceMouse.get.map((_, e)))
        .collect[KeyEvent]:
          case (false, e) => e
        .merge(forceKeyEvents)
        .tap(modificator.checkEvent)
        .foreach(k => modificator.state.flatMap(emulator.key(k, _)))
        .fork
      _ <- Console.readLine("PRESS ENTER TO EXIT ...")
    yield ()
  }
    .provide(
      Parameters.live,
      Modificator.live,
      Mouse.live,
      Emulator.live,
      GlobalActivator.live,
      Activator.live,
      Keys.live,
      Frame.live,
    )
