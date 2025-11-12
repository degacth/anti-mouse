package app.streams

import app.domain.Key
import app.layer.{Emulator, Move, Screen}
import zio.*
import zio.stream.ZStream

object Mouse:
  import ZIO.*

  import java.awt.event.KeyEvent.*

  enum MouseEvents:
    case FastMove, DirectionMove, Click, Mute

  private val fastMoveKeys: Chunk[Chunk[Int]] = Chunk(
    Chunk(VK_Q, VK_W, VK_E, VK_R),
    Chunk(VK_A, VK_S, VK_D, VK_F),
    Chunk(VK_Z, VK_X, VK_C, VK_V),
  )

  private val moveDirections = Map(
    VK_H -> Move.Direction.left,
    VK_J -> Move.Direction.down,
    VK_K -> Move.Direction.up,
    VK_L -> Move.Direction.right,
  )

  private val moveKeys = Chunk.fromIterable(moveDirections.keys)

  private val fastMoveKeysFlatten: Chunk[Int] = fastMoveKeys.flatten
  private val isFastMove: Int => Boolean = fastMoveKeysFlatten.contains(_)
  private val fastMoveDimension = (fastMoveKeys.headOption.fold(0)(_.size), fastMoveKeys.size)
  private val isMoveKey: Int => Boolean = moveKeys.contains(_)

  val streamKeyResolver: Key => MouseEvents =
    case Key.Pressed(code) if isFastMove(code) => MouseEvents.FastMove
    case k: Key if isMoveKey(k.code) => MouseEvents.DirectionMove
    case _ => MouseEvents.Mute

  private type OutDeps = Screen.Service & Emulator.Service & Screen.Display

  def handlers: Map[MouseEvents, ZStream[Any, Throwable, Key] => ZStream[OutDeps, Throwable, Any]] = Map(
    MouseEvents.FastMove -> (_.changes.mapZIO: pressed => // TODO why just pressed
      for
        (x, y) <- serviceWithZIO[Screen.Service]:
          _.screenPart(fastMoveDimension, fastMovePointByKey(pressed.code))
        _ <- serviceWithZIO[Emulator.Service](_.absolute(x, y))
      yield pressed
      ),

    MouseEvents.DirectionMove -> (
      _
        .tap:
          case Key.Pressed(code) =>
            serviceWithZIO[Emulator.Service]:
              _.moveStart(moveDirections.getOrElse(code, Move.Direction.empty)).fork *> unit
          case Key.Released(code) =>
            serviceWithZIO[Emulator.Service]:
              _.moveStop(moveDirections.getOrElse(code, Move.Direction.empty))
      ),

    MouseEvents.Mute -> (_.drain)
  )

  def fastMovePointByKey(key: Int): (Int, Int) = fastMoveKeys
    .zipWithIndex
    .find:
      case (ks, _) => ks.contains(key)
    .map:
      case (ks, i) => (ks.indexOf(key), i)
    .fold((0, 0)):
      case (x, y) => (x, y)
