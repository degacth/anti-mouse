package app.common

import scala.language.reflectiveCalls

object BinStore:
  opaque type State[T <: Enumic] = Int

  private type Enumic = {
    def ordinal: Int
  }

  def empty[T <: Enumic]: State[T] = 0
  def raw[T <: Enumic]: Int => State[T] = identity
  private def of[T <: Enumic]: Enumic => State[T] = _.bin

  extension [T <: Enumic](e: T)
    def bin: Int = 1 << e.ordinal
    def ++(o: T): State[T] = of(e) + o
    def state: State[T] = of(e)

  extension [T <: Enumic](s: State[T])
    def +(o: T): State[T] = s | o.bin
    def -(o: T): State[T] = (s | o.bin) ^ o.bin
    def ?(o: T): Boolean = (s & o.bin) != 0
    def |(o: State[T]): Boolean = (s | o) != 0
    def &(o: State[T]): Boolean = (s & o) == o
