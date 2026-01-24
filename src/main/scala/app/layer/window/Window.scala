package app.layer.window

import zio.*
import zio.ZIO.*
import zio.stream.{UStream, ZStream}

import java.awt.*
import java.awt.event.{KeyAdapter, KeyEvent, KeyListener}
import javax.swing.*

object Window:
  trait Service:
    def activate: UIO[Unit]
    def deactivate: UIO[Unit]
    def keys: ZStream[Any, Throwable, (Int, Promise[Nothing, Unit])]

  trait Frame:
    def addKeyListener(l: KeyListener): Unit = ()
    def activate: UIO[Unit] = unit
    def deactivate: UIO[Unit] = unit

  def frame: ULayer[Frame] = ZLayer.scoped:
    for
      jFrame <- acquireRelease(succeed(JFrame()) <* debug("frame created")): frm =>
        succeed(frm.dispose()) <* debug("frame disposed")
      _ <- succeed:
        import jFrame.*

        setFocusable(true)
        setTitle("AntiMouse")
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
        setUndecorated(true)
        setAlwaysOnTop(true)
        val size = 8
        setBounds(Rectangle(size, size, size, size))
        getContentPane.setBackground(Color.MAGENTA)
        setVisible(false)
    yield new Frame:
      override def addKeyListener(l: KeyListener): Unit = jFrame.addKeyListener(l)
      override def activate: UIO[Unit] = succeed(jFrame.setVisible(true))
      override def deactivate: UIO[Unit] = succeed(jFrame.setVisible(false))

  def keyEventStream: ZLayer[Frame, Nothing, UStream[KeyEvent]] = ZLayer.scoped:
    serviceWith[Frame]: frame =>
      ZStream.asyncScoped[Any, Nothing, KeyEvent] { cb =>
        succeed:
          frame.addKeyListener(new KeyAdapter:
            override def keyPressed(e: KeyEvent): Unit = cb(succeed(Chunk.single(e)))
            override def keyReleased(e: KeyEvent): Unit = cb(succeed(Chunk.single(e)))
          )
      }.changesWith((e1, e2) => e1.getKeyCode == e2.getKeyCode && e1.getID == e2.getID)

  def live: ZLayer[Frame & UStream[KeyEvent], Throwable, Service] = ZLayer.scoped:
    for
      frame <- service[Frame]
      keyEvents <- service[UStream[KeyEvent]]
      keyEventStream = keyEvents.groupByKey(_.getKeyCode):
        (k, s) =>
          s.mapAccumZIO(Option.empty[Promise[Nothing, Unit]]) {
              case (Some(p), e) if e.getID == KeyEvent.KEY_RELEASED => p.succeed(()).map(_ => (None, None))
              case (None, e) if e.getID == KeyEvent.KEY_PRESSED =>
                Promise.make[Nothing, Unit].map(p => (Some(p), Some((k, p))))
              case (s, _) => succeed((s, None))
            }
            .collect:
              case Some((k, p)) => (k, p)

    yield new Service:
      export frame.{activate, deactivate}
      override def keys: ZStream[Any, Throwable, (Int, Promise[Nothing, Unit])] = keyEventStream
