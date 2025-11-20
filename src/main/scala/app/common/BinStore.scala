package app.common

import scala.language.reflectiveCalls

object BinStore:
  opaque type State = Int

  private type Enumic = {
    def ordinal: Int
  }

  val empty: State = 0
  val raw: Int => State = identity
  private val of: Enumic => State = _.bin

  extension (e: Enumic)
    def bin: Int = 1 << e.ordinal
    def +(o: Enumic): State = of(e) + o
    def state: State = of(e)

  extension (s: State)
    def +(o: Enumic): State = s | o.bin
    def -(o: Enumic): State = (s | o.bin) ^ o.bin
    def ?(o: Enumic): Boolean = (s & o.bin) != 0
    def |(o: State): Boolean = (s | o) != 0
    def &(o: State): Boolean = (s & o) == o
