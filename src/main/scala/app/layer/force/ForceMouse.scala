package app.layer.force

import zio.*

object ForceMouse:
  trait Service

  val live: ZLayer[Any, Nothing, Service] = ???
