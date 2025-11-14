package app.layer

import zio.*
import ZIO.*

object Modificator:
  import app.common.BinaryCombinator.*

  enum Mode:
    case Alt, Ctrl, Shift
    val code: Int = this.ordinal.bin

  object Mode:
    def hasModes(state: Int, modes: Mode*): Boolean = modes.foldRight[Boolean](true)((m, acc) => (state ?& m.code) && acc)

  trait Service:
    def on: Mode => UIO[Unit]
    def off: Mode => UIO[Unit]
    def has: Mode => UIO[Boolean]
    def watch: Queue[Int]

  val live: ULayer[Service] = ZLayer.fromZIO:
    for
      state <- Ref.make(0)
      queue <- Queue.unbounded[Int]
    yield new Service:
      override val on: Mode => UIO[Unit] = m => state.updateAndGet(m.code +& _).flatMap(offer)
      override val off: Mode => UIO[Unit] = m => state.updateAndGet(m.code -& _).flatMap(offer)
      override val has: Mode => UIO[Boolean] = m => state.get.map(m.code ?& _)
      override val watch: Queue[Int] = queue

      private def offer: Int => UIO[Unit] = queue.offer(_).unit
