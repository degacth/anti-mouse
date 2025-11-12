package app.layer

import zio.*

object Move:
  import ZIO.*

  class Speed(val v: Int) extends AnyVal
  class Rate(val v: Int) extends AnyVal

  def speed: Int => Speed = Speed(_)
  def rate: Int => Rate = Rate(_)

  enum Direction(val x: Int, val y: Int, bindex: Int):
    private case Up extends Direction(0, -1, 1 << 0)
    private case Right extends Direction(1, 0, 1 << 1)
    private case Down extends Direction(0, 1, 1 << 2)
    private case Left extends Direction(-1, 0, 1 << 3)
    private case Empty extends Direction(0, 0, 0)

    val direction: (Int, Int) = (x, y)
    val save: Int => Int = _ | bindex
    val remove: Int => Int = s => s ^ bindex
    val in: Int => Boolean = n => (n & bindex) != 0

  object Direction:
    val up: Direction = Up
    val right: Direction = Right
    val down: Direction = Down
    val left: Direction = Left
    val empty: Direction = Empty

  trait Service:
    def run(d: Direction, z: (Int, Int) => UIO[Unit]): UIO[Unit]
    def stop(d: Direction): UIO[Unit]

  val live: URLayer[Speed & Rate, Service] = ZLayer.scoped:
    for
      directions <- Ref.make((0, (0, 0)))
      speed <- service[Speed]
      rate <- service[Rate]
    yield
      new Service:
        private def execute(z: (Int, Int) => UIO[Unit]): UIO[Unit] =
          for
            (s, (x, y)) <- directions.get
            _ <-
              if s == 0 then unit
              else z(x * speed.v, y * speed.v) *> sleep(rate.v.millis) *> execute(z)
          yield ()

        override def run(d: Direction, z: (Int, Int) => UIO[Unit]): UIO[Unit] =
          for
            (s, _) <- directions.getAndUpdate:
              case (state, (x, y)) if !(d in state) => (d save state, (x + d.x, y + d.y))
              case s => s
            _ <- if s == 0 then execute(z) else unit
          yield ()

        override def stop(d: Direction): UIO[Unit] =
          for
            _ <- directions.update:
              case (state, (x, y)) if d in state => (d remove state, (x - d.x, y - d.y))
              case m => m
          yield ()
