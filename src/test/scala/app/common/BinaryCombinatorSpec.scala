package app.common

import zio.*
import ZIO.*
import zio.test.*

object BinaryCombinatorSpec extends ZIOSpecDefault:
  import BinaryCombinator.*
  import utils.Specs.*

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("binary combinator spec")(
    test("should convert int to binary"):
      withFixtures(List(
        (1, 2),
        (4, 16),
        (9, 512),
      ))(n => succeed(n.bin))
    ,

    test("should add value to state"):
      withFixtures(List(
        ((1, 2), 3),
        ((3, 4), 7),
        ((7, 8), 15),
        ((9, 8), 9),
      )): (s, b) =>
        succeed(b +& s)
    ,

    test("should exclude value from state"):
      withFixtures(List(
        ((4, 1), 4),
        ((7, 2), 5),
      )): (s, b) =>
        succeed(b -& s)
    ,

    test("should check is number in state"):
      withFixtures(List(
        ((4, 3), false),
      )): (b, s) =>
        succeed(b ?& s)
    ,
  )
