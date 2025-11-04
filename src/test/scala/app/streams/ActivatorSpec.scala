package app.streams

import zio.*
import zio.test.*
import stubs.GlobalKeyListenerStub

object ActivatorSpec extends ZIOSpecDefault:
  import ZIO.*

  private def withActivator(f: GlobalKeyListenerStub => UIO[Unit]): URIO[GlobalKeyListenerStub, Chunk[Activator.Message]] =
    val streamTimeout = 10
    for
      fiber <- Activator.toggler.timeout(streamTimeout.millis).runCollect.orDie.fork
      stub <- service[GlobalKeyListenerStub]
      _ <- f(stub).delay(1.millis).fork
      _ <- TestClock.adjust((streamTimeout + 1).millis)
      act <- fiber.join
    yield act

  def spec = suite("activator spec")(
    test("should emit toggle event"):
      withActivator { stub =>
        stub.pressed(Activator.pressToToggle.init *) *>
          stub.pressed(Activator.pressToToggle.last)
      }
        .map(act => assertTrue(act == Chunk.single(Activator.Message.Toggle)))
    ,

    test("should not emit toggle"):
      withActivator { stub =>
        stub.pressed(Activator.pressToToggle.init *) *>
          stub.released(Activator.pressToToggle.head) *>
          stub.pressed(Activator.pressToToggle.last)
      }
        .map(act => assertTrue(act == Chunk.empty))
    ,

    test("should emit when released"):
      val (first, last) = (
        Activator.pressToToggle.head,
        Activator.pressToToggle.last,
      )

      val twoButtons = first :: last :: Nil

      withActivator { stub =>
        stub.pressed(twoButtons *) *>
          stub.released(Activator.pressToToggle *) *>
          stub.pressed(twoButtons *)
      }
        .map(act => assertTrue(act == Chunk.empty))
  )
    .provideLayer(GlobalKeyListenerStub.live)
