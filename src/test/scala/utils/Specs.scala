package utils

import zio.*
import zio.test.*

object Specs:
  def withFixtures[F, R, E, A](fixtures: List[(F, A)])(f: F => ZIO[R, E, A]) =
    ZIO
      .foreach(fixtures):
        case (fixture, expected) => f(fixture).map((_, expected))
      .map: results =>
        assert(results):
          Assertion.forall(Assertion.assertion("assert") {
            case (act, exp) => act == exp
          })
