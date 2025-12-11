package app

import app.layer.{Cursor, Emulator, Modificator, Move, Screen, Window2}
import app.layer.activator.{Activator2, GlobalKeyListener}
import app.streams.{Mouse, Window}
import zio.*
import ZIO.*
import app.layer.emulator.{DirectionMove, FastMove, KeysEmulator}
import app.layer.modificator.KeyModificator
import zio.stream.{ZPipeline, ZSink, ZStream}

object Main extends ZIOAppDefault:
  extension [R, E, A](s: ZStream[R, E, A])
    def >>> [B] (f: ZStream[R, E, A] => ZStream[R, E, B]): ZStream[R, E, B] = f(s)

  private val application =
    for
      activator <- service[Activator2.Service]
      window <- service[Window2.Service]
      emulator <- service[KeysEmulator.Service]
      cursor <- service[Cursor.Service]

      _ <- (window.keys >>> emulator.emulate >>> cursor.move)
        .runDrain
        .catchAllCause(k => logError(k.prettyPrint))
        .fork
      _ <- activator.toggler.foreach(_ => window.toggleVisibility)
    yield ()

  override def run: Task[ExitCode] = scoped:
    for
      fiber <- application
        .provide(
          Window2.live,
          Activator2.live,
          GlobalKeyListener.live,
          KeysEmulator.live,
          FastMove.live,
          DirectionMove.live,
          Screen.live,
          Screen.display,
          Cursor.live,
          KeyModificator.live,
        )
        .catchAllCause(e => logError(e.prettyPrint))
        .fork
      _ <- Console.readLine
      _ <- fiber.interrupt
    yield ExitCode.success

/*
private val application =
  for
    window <- service[Window.Service]
    _ <- {
      (Activator.toggler.merge(window.commands) >>> Window.activator).merge:
        Window.keyboardStream
          .groupByKey(Mouse.streamKeyResolver): (k, s) =>
            Mouse.handlers
              .get(k)
              .fold(s >>> ZPipeline.mapZIO(m => ZIO.debug(s"unhandled key $k with message $m")))
              .apply(_(s))
    }
      .catchAllCause(c => ZStream.fromZIO(ZIO.debug(c.prettyPrint)))
      .runDrain
  yield ()

override def run: Task[ExitCode] = ZIO.scoped:
  for
    fiber <- application
      .provide(
        Activator.globalKeyListener,
        Window.frame,
        Emulator.live,
        Screen.live,
        Screen.display,
        Move.live,

        Modificator.live,
        ZLayer.succeed(Move.speed(3)),
        ZLayer.succeed(Move.rate(16)),
      )
      .catchAll(ZIO.debug(_))
      .fork
    _ <- Console.readLine
    _ <- fiber.interrupt
  yield ExitCode.success


 */
