package app.layer.emulator

import zio.*
import ZIO.*
import zio.test.*

import java.awt.event.InputEvent

object ModificatorSpec extends ZIOSpecDefault:
  import java.awt.event.KeyEvent
  import javax.swing.JButton

  override def spec: Spec[TestEnvironment & Scope, Any] = suite(getClass.getSimpleName)(
    test("should save modifier pressed when other released") {
      val pressAlt = StubbedKey(KeyEvent.KEY_PRESSED, KeyEvent.VK_ALT)
      val pressShiftModAlt = StubbedKey(KeyEvent.KEY_PRESSED, KeyEvent.VK_SHIFT, InputEvent.ALT_DOWN_MASK)
      val releaseAltModShift = StubbedKey(KeyEvent.KEY_RELEASED, KeyEvent.VK_ALT, InputEvent.SHIFT_DOWN_MASK)

      for
        modificator <- service[Modificator.Service]
        _ <- TestClock.adjust(100.millis)
        _ <- ZIO.foreachDiscard(Chunk(
          pressAlt,
          pressShiftModAlt,
          releaseAltModShift,
        ))(modificator.checkEvent)
        alt <- modificator.hasAlt
        shift <- modificator.hasShift
      yield assertTrue(shift) && !assertTrue(alt)
    }
  ).provide(Modificator.live) @@ TestAspect.timeout(2.seconds)

  case class StubbedKey(e: Int, code: Int, mod: Int = 0) extends KeyEvent(JButton(), e, 0, mod, code, Char.MinValue)
