package app.layer

import zio.*
import zio.test.*

object MoveSpec extends ZIOSpecDefault:
  import ZIO.*

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("move spec")(
    test("should start move"):
      for
        mock <- service[MovePageObject.Service]
        fiber <- mock.up.fork
        _ <- TestClock.adjust(300.millis)
        actual <- mock.moved
        _ <- assertTrue(actual == (0, -1))

        _ <- mock.unup
        _ <- TestClock.adjust(1.seconds)
        _ <- fiber.join
        c <- mock.count
      yield assertTrue(c == 4)
    ,

    test("should handle many keys"):
      for
        mock <- service[MovePageObject.Service]
        _ <- mock.up.fork
        _ <- mock.left.fork
        _ <- TestClock.adjust(200.millis)
        actual <- mock.moved
        _ <- assertTrue(actual == (-1, -1))

        _ <- mock.unup.fork
        _ <- TestClock.adjust(100.millis)
        actual <- mock.moved
      yield assertTrue(actual == (-1, 0))
    ,

    test("should set speed") {
      for
        mock <- service[MovePageObject.Service]
        _ <- mock.left.fork
        _ <- TestClock.adjust(200.millis)
        actual <- mock.moved
      yield assertTrue(actual == (-3, 0))
    }
      .provideLayer:
      speed(3) >>> Move.live >>> MovePageObject.live
  )
    .provide(
      rate(100),
      speed(1),
      Move.live,
      MovePageObject.live,
    )

val rate: Int => ULayer[Move.Rate] = n => ZLayer.succeed(Move.rate(n))
val speed: Int => ULayer[Move.Speed] = n => ZLayer.succeed(Move.speed(n))

object MovePageObject:
  trait Service:
    def up: UIO[Unit]
    def unup: UIO[Unit]

    def left: UIO[Unit]
    def unleft: UIO[Unit]

    def count: UIO[Int]
    def moved: UIO[(Int, Int)]

  val live: URLayer[Move.Service, Service] = ZLayer.scoped:
    for
      sut <- ZIO.service[Move.Service]
      state <- Ref.make(0 -> 0 -> 0)
    yield new Service:
      def up: UIO[Unit] = runKey(Move.Direction.up)
      def unup: UIO[Unit] = stopKey(Move.Direction.up)

      def left: UIO[Unit] = runKey(Move.Direction.left)
      def unleft: UIO[Unit] = stopKey(Move.Direction.left)

      def count: UIO[Int] = state.get.map((_, c) => c)
      def moved: UIO[(Int, Int)] = state.get.map((v, _) => v)

      private val runKey: Move.Direction => UIO[Unit] = d => sut.run(d, (x, y) => state.update(v => x -> y -> (v._2 + 1)))
      private val stopKey: Move.Direction => UIO[Unit] = sut.stop
