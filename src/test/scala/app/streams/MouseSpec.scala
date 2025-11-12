package app.streams

import utils.Specs
import zio.*
import zio.ZIO.*
import zio.test.*

import java.awt.event.KeyEvent.*

object MouseSpec extends ZIOSpecDefault:
  def spec = suite("mouse spec")(
    test("should get fast move point"):
      Specs.withFixtures(List(
        (VK_Q, (0, 0)),
        (VK_V, (3, 2)),
        (VK_S, (1, 1)),
        (VK_P, (0, 0)),
      )): fixture =>
        succeed(Mouse.fastMovePointByKey(fixture))
    ,
  )
