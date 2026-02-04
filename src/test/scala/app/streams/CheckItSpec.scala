package app.streams

import zio.*
import zio.stream.ZStream
import zio.test.*

object CheckItSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment & Scope, Any] = suite(getClass.getSimpleName)(
    test("switch map"):
      assertTrue(true)
  )
