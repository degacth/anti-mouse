package app.streams

import app.layer.{Emulator, Screen}
import zio.*
import zio.stream.ZPipeline

object Mouse:
  import java.awt.event.KeyEvent.*
  import ZIO.*

  private val fastMoveKeys: Chunk[Chunk[Int]] = Chunk(
    Chunk(VK_Q, VK_W, VK_E, VK_R),
    Chunk(VK_A, VK_S, VK_D, VK_F),
    Chunk(VK_Z, VK_X, VK_C, VK_V),
  )

  private val fastMoveDimension = (fastMoveKeys.headOption.fold(0)(_.size), fastMoveKeys.size)
  private val handledKeys = fastMoveKeys.flatten

  def fastMovePointByKey(key: Int): (Int, Int) = fastMoveKeys
    .zipWithIndex
    .find:
      case (ks, _) => ks.contains(key)
    .map:
      case (ks, i) => (ks.indexOf(key), i)
    .fold((0, 0)):
      case (x, y) => (x, y)

  type Deps = Emulator.Service & Screen.Service & Screen.Display

  def keysToMouse: ZPipeline[Deps, Throwable, Window.Key, Any] = ZPipeline
    .filter:
      case Window.Key(code) => handledKeys.contains(code)
    .mapZIO:
      case pressed: Window.Key.Pressed =>
        for
          (x, y) <- serviceWithZIO[Screen.Service]:
            _.screenPart(fastMoveDimension, fastMovePointByKey(pressed.code))
          _ <- serviceWithZIO[Emulator.Service](_.absolute(x, y))
        yield ()
      case released: Window.Key.Released => unit
