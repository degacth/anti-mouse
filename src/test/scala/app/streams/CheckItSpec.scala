package app.streams

import zio.*
import zio.stream.ZStream
import zio.test.*

object CheckItSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment & Scope, Any] = suite(getClass.getSimpleName)(
    test("switch map"):
      for
        _ <- ZIO.unit
        s1 = ZStream.range(1, 10)
        s2 = s1.map(_ * 100)
        actual <- s1
          .mergeEither(s2)
          .collectZIO: v =>
            for
              cond <- ZIO.succeed(true)
            yield ???
          .runCollect
      yield assertTrue(actual == Chunk.empty)
  )
