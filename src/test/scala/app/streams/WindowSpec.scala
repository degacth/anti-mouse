package app.streams

import app.layer.window.Window
import zio.*
import ZIO.*
import zio.stream.*
import zio.test.*

import java.awt.event.KeyEvent
import javax.swing.JButton

object WindowSpec extends ZIOSpecDefault:
  val keyEventsStub = ZLayer.succeed[UStream[KeyEvent]](ZStream(
    StubbedKey(KeyEvent.KEY_PRESSED, 0)
  ))

  val frameStub = ZLayer.succeed(new Window.Frame {})

  val testKeysStub = ZLayer.succeed[UStream[KeyEvent]](ZStream(
    StubbedKey(KeyEvent.KEY_PRESSED, 1000)
  ))

  override def spec: Spec[TestEnvironment & Scope, Any] = suite(getClass.getSimpleName)(
    test("should emit keys promise in right order") {
      for
        window <- ZIO.service[Window.Service]
        actual <- window.keys.runCollect
      yield assertTrue(actual == Chunk(1, 2))
    }.provideSomeLayer[Window.Frame](testKeysStub >>> Window.live)
  )
    .provide(frameStub)

  case class StubbedKey(e: Int, code: Int) extends KeyEvent(JButton(), e, 0, 0, code, Char.MinValue)
