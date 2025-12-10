package app.layer.emulator

import zio.ZLayer
import zio.*
import ZIO.*
import app.domain.{CursorAction, Direction}

import java.awt.event.KeyEvent

object DirectionMove:
  import KeyEvent.*

  private val keysByDirection = Map(
    VK_H -> Direction.Left,
    VK_J -> Direction.Down,
    VK_K -> Direction.Up,
    VK_L -> Direction.Right,
  )
  private val keys = Chunk.fromIterable(keysByDirection.keys)

  trait Service:
    infix def has(e: KeyEvent): Boolean = keys.contains(e.getKeyCode)
    def start(code: Int): CursorAction.StartMove = CursorAction.StartMove(keysByDirection(code))
    def stop(code: Int): CursorAction.StopMove = CursorAction.StopMove(keysByDirection(code))

  val live: ZLayer[Any, Throwable, Service] = ZLayer.succeed:
    new Service {}
