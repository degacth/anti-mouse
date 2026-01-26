package app.streams

import app.layer.window.{Frame, Window}
import zio.*
import zio.ZIO.*
import zio.stream.*
import zio.test.*

import java.awt.event.KeyEvent
import javax.swing.JButton

object WindowSpec extends ZIOSpecDefault:
  private val frameStub = ZLayer.succeed(new Frame.Service {})

  override def spec: Spec[TestEnvironment & Scope, Any] = suite(getClass.getSimpleName)(
    test("should emit keys promise in right order") {
      for
        window <- service[Window.Service]
        (k, p) <- window.keys.runHead.flatMap(fromOption)
        result <- p.await
      yield assertTrue(result == () && k == KeyEvent.VK_W)
    }
      .provideSomeLayer:
        ZLayer.succeed(
          ZStream(
            StubbedKey(KeyEvent.KEY_PRESSED, KeyEvent.VK_W),
            StubbedKey(KeyEvent.KEY_RELEASED, KeyEvent.VK_W),
            StubbedKey(KeyEvent.KEY_RELEASED, KeyEvent.VK_K),
          )
        ) >>> Window.live
    ,
  )
    .provide(
      frameStub
    ) @@ TestAspect.timeout(2.seconds)

  case class StubbedKey(e: Int, code: Int) extends KeyEvent(JButton(), e, 0, 0, code, Char.MinValue)
