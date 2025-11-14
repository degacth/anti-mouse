package app.common

object BinaryCombinator:
  extension (i: Int)
    def bin: Int = 1 << i
    def +&(s: Int): Int = s | i
    def -&(s: Int): Int = (s | i) ^ i
    def ?&(s: Int): Boolean = (i & s) != 0
