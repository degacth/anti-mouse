package app.layer

import app.streams.Window
import zio.*
import zio.stream.ZStream

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

  private type Deps = Speed & Rate & Modificator.Service & Window.Service

  private enum SpeedMod:
    case Slow, Normal, Fast, Faster

    lazy val speed: Double = this match
      case Slow => .6
      case Normal => 1
      case Fast => 2
      case Faster => 3

  private case class State(x: Int, y: Int, dir: Int, speedMod: SpeedMod)

  private object State:
    val empty: State = State(0, 0, 0, SpeedMod.Normal)

  val live: URLayer[Deps, Service] = ZLayer.scoped:
    import Modificator.Mode.*
    for
      state <- Ref.make(State.empty)
      speed <- service[Speed]
      rate <- service[Rate]
      _ <- Window.focusStream
        .mapZIO:
          case Window.Focus.Lost => state.set(State.empty)
          case _ => unit
        .runDrain
        .fork
      modificator <- service[Modificator.Service]
      _ <- ZStream.fromQueue(modificator.watch)
        .runForeach: m =>
          state.update(_.copy(speedMod = m match
            case m if hasModes(m, Shift, Ctrl) => SpeedMod.Fast
            case m if hasModes(m, Shift) => SpeedMod.Faster
            case m if hasModes(m, Ctrl) => SpeedMod.Slow
            case _ => SpeedMod.Normal
          ))
        .fork
    yield
      new Service:
        private def execute(z: (Int, Int) => UIO[Unit]): UIO[Unit] =
          for
            State(x, y, dir, speedMod) <- state.get
            _ <-
              if dir == 0 then unit
              else z((x * speed.v * speedMod.speed).toInt, (y * speed.v * speedMod.speed).toInt) *>
                sleep(rate.v.millis) *>
                execute(z)
          yield ()

        override def run(d: Direction, z: (Int, Int) => UIO[Unit]): UIO[Unit] =
          for
            State(_, _, dir, _) <- state.getAndUpdate:
              case s@State(x, y, dir, _) if !(d in dir) => s.copy(x = x + d.x, y = y + d.y, dir = d save dir)
              case s => s
            _ <- if dir == 0 then execute(z) else unit
          yield ()

        override def stop(d: Direction): UIO[Unit] =
          for
            _ <- state.update:
              case s@State(x, y, dir, _) if d in dir => s.copy(x = x - d.x, y = y - d.y, dir = d remove dir)
              case m => m
          yield ()
