package app

import zio.*
import ZIO.*
import app.layer.activator.{Activator, GlobalActivator}
import app.layer.emulator.{Emulator, Modificator, Mouse}
import app.layer.window.{Frame, Keys}
import app.parameters.Parameters
import zio.stream.UStream
import zio.config.typesafe.TypesafeConfigProvider.*

import java.nio.file.Paths

object Main extends ZIOAppDefault:
  private val configFileName = ".anti-mouse.conf"

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.setConfigProvider(fromHoconFilePath("src/main/resources/application.conf"))

  override def run: Task[Any] = application.catchAllCause(c => logError(c.prettyPrint))

  private def application: Task[Any] = {
    for
      configDir <- System.property("user.home").orElse(System.property("user.dir"))
      configFile <- attempt:
        configDir.fold(Paths.get(".", configFileName))(Paths.get(_, configFileName))
      _ <- debug(configFile.toFile)
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

      _ <- serviceWithZIO[Keys.Stream](_.foreach(k => modificator.state.flatMap(s => emulator.key(k, s)))).fork
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
