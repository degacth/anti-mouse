package app.layer.emulator

import zio.*
import ZIO.*
import app.layer.window.Frame

object Emulator:
  trait Service:
    def move: (Int, Promise[Nothing, Unit]) => UIO[Unit]

  private val moveKeys =
    import Mouse.Direction.*
    import java.awt.event.KeyEvent.*

    Map(
      VK_W -> Up,
      VK_K -> Up,
      VK_D -> Right,
      VK_L -> Right,
      VK_S -> Down,
      VK_J -> Down,
      VK_A -> Left,
      VK_H -> Left,
    )

  private type Deps = Mouse.Service & Frame.Service

  def live: ZLayer[Deps, Throwable, Service] = ZLayer.scoped:
    for
      mouse <- service[Mouse.Service]
    yield new Service:
      override val move: (Int, Promise[Nothing, Unit]) => UIO[Unit] =
        case (k, p) if moveKeys.contains(k) => mouse.move(moveKeys(k), p)
        case _ => unit
