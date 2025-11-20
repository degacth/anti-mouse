package app.common

import zio.*
import ZIO.*
import utils.Specs
import zio.test.*

enum Other:
  case A, B, C

object BinStoreSpec extends ZIOSpecDefault:
  import BinStore.*

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("bi spec")(
    test("should match in case"):
      val s = Other.A + Other.B
      Specs.withFixtures(List(
        (s | (Other.A + Other.B), true),
        (s & (Other.C + Other.B) && !(s ? Other.A), false),
        (s | (Other.A + Other.C), true),
        (s ? Other.C, false),
      ))(ZIO.succeed)
    ,

    test("should add state with value"):
      val s0: State = Other.A.state
      val s1 = s0 + Other.B + Other.C
      val s2 = s1 - Other.A
      val s3 = s1 - Other.A
      for
        _ <- unit
      yield assertTrue(s1 == raw(7)) && assertTrue(s2 == raw(6)) && assertTrue(s3 == s2)
    ,
  )

