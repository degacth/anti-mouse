package app.parameters

import zio.*
import zio.config.*
import zio.config.magnolia.deriveConfig

case class Parameters(cursorSpeed: Int, moveRate: Int)

object Parameters:
  implicit val paramsDescriptor: Config[Parameters] = deriveConfig[Parameters]
  
  val live: ZLayer[Any, Throwable, Parameters] = ZLayer.scoped(ZIO.config[Parameters])
