package stubs

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
  override def focus: WindowFocusListener => UIO[Unit] = ???
  override def commands: UStream[Activator.Message] = ???
  
object WindowStub:
  val live: ULayer[Window.Service] = ZLayer.succeed(WindowStub())
