package app.streams

import app.domain.WindowEvent
import app.streams.Activator.HotKey
import zio.*
import zio.test.*
import stubs.GlobalKeyListenerStub

object ActivatorSpec extends ZIOSpecDefault:
  import ZIO.*

  private val pressToToggle = Seq(HotKey.Ctrl, HotKey.Alt, HotKey.Semicolon)

  private def withActivator(f: GlobalKeyListenerStub => UIO[Unit]): URIO[GlobalKeyListenerStub, Chunk[WindowEvent]] =
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
        stub.pressed(pressToToggle.init *) *>
          stub.pressed(pressToToggle.last)
      }
        .map(act => assertTrue(act == Chunk.single(WindowEvent.Toggle)))
    ,

    test("should not emit toggle"):
      withActivator { stub =>
        stub.pressed(pressToToggle.init *) *>
          stub.released(pressToToggle.head) *>
          stub.pressed(pressToToggle.last)
      }
        .map(act => assertTrue(act == Chunk.empty))
    ,

    test("should emit when released"):
      val (first, last) = (
        pressToToggle.head,
        pressToToggle.last,
      )

      val twoButtons = first :: last :: Nil

      withActivator { stub =>
        stub.pressed(twoButtons *) *>
          stub.released(pressToToggle *) *>
          stub.pressed(twoButtons *)
      }
        .map(act => assertTrue(act == Chunk.empty))
    ,

    /*
    test("should group keys"):
      import zio.stream.*

      for
        _ <- TestRandom.setSeed(42)
        fiber <- ZStream
          .iterate(0)(_ + 1)
          .mapZIOParUnordered(4)(n => Random.nextIntBetween(0, 50).flatMap(t => succeed(n).delay(t.millis)))
          .take(12)
          .groupByKey(_ % 3 == 0)((_, s) => ZStream(s))
          .rechunk(2)
          .mapChunksZIO:
            case Chunk(s1, s2, _*) => succeed(Chunk.single(s1.zipAllWith(s2)((_, -1), (-2, _))((_, _))))
            case _ => fail(new IllegalStateException(">>> Wrong regrouping keyboard events <<<"))
          .flatten
          .runCollect
          .fork
        _ <- TestClock.adjust(2.seconds)
        actual <- fiber.join
      yield assertTrue(actual == Chunk(0, 1, 2, 3))
     */

  )
    .provideLayer(GlobalKeyListenerStub.live)
