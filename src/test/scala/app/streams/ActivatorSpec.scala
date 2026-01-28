package app.streams

import app.layer.activator.Activator
import app.layer.activator.Activator.Status.{Activated, Deactivated}
import zio.*
import zio.stream.ZStream
import zio.test.*

object ActivatorSpec extends ZIOSpecDefault:
  import ZIO.*

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("activator spec")(
    test("should activate application") {
      for
        sut <- service[Activator.Service]
        actual <- sut.stream.runCollect
      yield assertTrue(actual == Chunk(Activated))
    }
      .provide(
        ZLayer.succeed(ZStream(
          Activator.Status.Activated,
          Activator.Status.Deactivated,
          Activator.Status.Activated,
        )),
        Activator.live,
      )
    ,

    test("should not activate application") {
      for
        sut <- service[Activator.Service]
        fiber <- sut.stream.runCollect.fork
        _ <- TestClock.adjust(600.millis)
        actual <- fiber.join
      yield assertTrue(actual == Chunk())
    }
      .provide(
        ZLayer.succeed(ZStream(Activator.Status.Activated, Activator.Status.Deactivated) ++ ZStream.fromZIO(
          succeed(Activator.Status.Activated).delay((Activator.actionTimeout + 1).millis)
        )),
        Activator.live,
      )
  ) @@ TestAspect.timeout(1.second)
