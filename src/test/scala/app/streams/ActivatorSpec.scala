package app.streams

import app.layer.activator.Activator
import app.layer.activator.Activator.Status.{Activated, Deactivated}
import zio.*
import zio.test.*
import stubs.GlobalKeyListenerStub

object ActivatorSpec extends ZIOSpecDefault:
  import ZIO.*

  import Activator.{actionTimeout, actionKey}

  def spec = suite("activator spec")(
    test("should activate application"):
      for
        interrupter <- Promise.make[Throwable, Unit]
        stub <- service[GlobalKeyListenerStub]
        sut <- service[Activator.Service]

        fiber <- sut
          .stream
          .interruptWhen(interrupter)
          .runCollect
          .fork
        _ <- TestClock.adjust(1.millis)

        _ <- stub.pressed(actionKey)
          *> stub.released(actionKey)
          *> stub.pressed(actionKey)
        _ <- TestClock.adjust(1.millis)
        _ <- interrupter.succeed(())
        actual <- fiber.join
      yield assertTrue(actual == Chunk(Deactivated, Activated))
    ,
  )
    .provide(
      GlobalKeyListenerStub.live,
      Activator.live,
    ) @@ TestAspect.timeout(1.second)
