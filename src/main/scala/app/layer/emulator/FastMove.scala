package app.layer.emulator

import zio.*
import ZIO.*
import app.domain.CursorAction
import app.layer.Screen

import java.awt.event.KeyEvent

object FastMove:
  import KeyEvent.*

  trait Service:
    infix def has(e: KeyEvent): Boolean
    def absolute(e: KeyEvent): Task[CursorAction.Absolute]

  private val keys: Chunk[Chunk[Int]] = Chunk(
    Chunk(VK_Q, VK_W, VK_E, VK_R),
    Chunk(VK_A, VK_S, VK_D, VK_F),
    Chunk(VK_Z, VK_X, VK_C, VK_V),
  )

  private val flatKeys = keys.flatten
  private val dimension = (keys.headOption.fold(0)(_.size), keys.size)
  private def has(e: KeyEvent): Boolean = flatKeys.contains(e.getKeyCode)

  private def fastMovePointByKey(key: Int): (Int, Int) = keys
    .zipWithIndex
    .find:
      case (ks, _) => ks.contains(key)
    .map:
      case (ks, i) => (ks.indexOf(key), i)
    .fold((0, 0)):
      case (x, y) => (x, y)

  val live: ZLayer[Screen.Service, Throwable, Service] = ZLayer.scoped:
    for
      screen <- service[Screen.Service]
    yield new Service:
      override def has(e: KeyEvent): Boolean = FastMove.has(e)
      override def absolute(e: KeyEvent): Task[CursorAction.Absolute] = screen
        .screenPart(dimension, fastMovePointByKey(e.getKeyCode))
        .map(CursorAction.Absolute(_, _))
