package stubs

import app.layer.{Emulator, Move}
import zio.*

class EmulatorStub extends Emulator.Service:

  import EmulatorStub.EmulatedMove

  var moves: Chunk[EmulatedMove] = Chunk.empty

  override def absolute(x: Int, y: Int): UIO[Unit] = addMove(EmulatedMove.Absolute(x, y))

  private val addMove: EmulatedMove => UIO[Unit] = m => ZIO.succeed:
    moves = moves ++ Chunk.single(m)
  override def moveStart: Move.Direction => UIO[Unit] = ???
  override def moveStop: Move.Direction => UIO[Unit] = ???

object EmulatorStub:
  enum EmulatedMove:
    case Absolute(x: Int, y: Int)
    case Direction

  val live: ULayer[Emulator.Service & EmulatorStub] = ZLayer.succeed(EmulatorStub())
