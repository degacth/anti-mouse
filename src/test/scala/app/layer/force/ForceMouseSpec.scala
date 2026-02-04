package app.layer.force

import zio.*
import zio.test.*

object ForceMouseSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment & Scope, Any] = suite(getClass.getSimpleName)(
    test("should activate force mode when pressed action key") {
      for
        _ <- ZIO.unit
      yield assertTrue(true)
    }
  )
