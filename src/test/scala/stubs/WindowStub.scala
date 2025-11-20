package stubs

import app.domain.WindowEvent
import app.streams.{Activator, Window}
import zio.*
import zio.stream.UStream

import java.awt.event.{KeyListener, WindowFocusListener}

class WindowStub extends Window.Service:
  override def activate: UIO[Unit] = ???
  override def deactivate: UIO[Unit] = ???
  override def show: UIO[Unit] = ???
  override def hide: UIO[Unit] = ???
  override def listen: KeyListener => UIO[Unit] = ???
  override def focus: WindowFocusListener => UIO[Unit] = _ => ZIO.unit
  override def commands: UStream[WindowEvent] = ???

object WindowStub:
  val live: ULayer[Window.Service] = ZLayer.succeed(WindowStub())
