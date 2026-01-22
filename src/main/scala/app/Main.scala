package app

import zio.*
import ZIO.*
import app.layer.activator.{Activator, GlobalKeyListener}

object Main extends ZIOAppDefault:
  override def run: Task[Any] = application

  private def application: Task[Any] =
    (for
      f <- serviceWithZIO[Activator.Service]: s =>
        s.stream
          .foreach:
            case Activator.Status.Activated => debug("ACTIVE")
            case Activator.Status.Deactivated => debug("Inactive")
          .fork

      _ <- Console.readLine("ENTER to stop")
      _ <- f.interrupt
    yield ()
      ).provide(Activator.live, GlobalKeyListener.live)
