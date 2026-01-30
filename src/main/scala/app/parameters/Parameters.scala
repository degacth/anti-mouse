package app.parameters

import zio.*
import zio.config.magnolia.deriveConfig
import zio.config.typesafe.TypesafeConfigProvider.fromHoconFile

import java.nio.file.{Files, Paths}

case class Parameters(cursorSpeed: Int, moveRate: Int)

object Parameters:
  private val paramsFilename = ".anti-mouse.conf"
  implicit val paramsDescriptor: Config[Parameters] = deriveConfig[Parameters]

  val live: ZLayer[Any, Throwable, Parameters] = ZLayer.scoped(ZIO.config[Parameters])

  val configProvider: ZLayer[Any, Throwable, Unit] =
    import ZIO.*

    ZLayer.scoped:
      for
        dir <- System.property("user.home").orElse(System.property("user.dir"))
        file <- attempt(Paths.get(dir.fold("")(identity), paramsFilename).toFile)
        _ <- whenCaseZIO(attempt(file.exists())): // read defaults from resource and write it
          case false =>
            fromAutoCloseable:
              attempt:
                this.getClass.getClassLoader.getResourceAsStream("application.conf")
            .flatMap(is => attempt(Files.copy(is, file.toPath)))
        provider <- withConfigProviderScoped(fromHoconFile(file))
        _ <- debug(s"configuration load from $file")
      yield provider
