package app.layer.window

import zio.*
import ZIO.*
import zio.stream.{UStream, ZStream}

import java.awt.*
import java.awt.event.{KeyAdapter, KeyEvent}
import javax.swing.*

object Window:
  trait Service:
    def activate: UIO[Unit]
    def deactivate: UIO[Unit]
    def keyPress: ZStream[Any, Throwable, KeyEvent]
    def keyRelease: ZStream[Any, Throwable, KeyEvent]
    def keys: ZStream[Any, Throwable, (Int, Promise[Nothing, Unit])]

  def live: ZLayer[Any, Throwable, Service] = ZLayer.scoped:
    for
      frame <- acquireRelease(attempt(JFrame()) <* debug("frame created")): frm =>
        succeed(frm.dispose()) <* debug("frame disposed")
      _ <- attempt:
        import frame.*

        setFocusable(true)
        setTitle("AntiMouse")
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
        setUndecorated(true)
        setAlwaysOnTop(true)
        val size = 8
        setBounds(Rectangle(size, size, size, size))
        getContentPane.setBackground(Color.MAGENTA)
        setVisible(false)

      p <- Promise.make[Nothing, Unit]

      _ = ZStream.asyncScoped[Any, Throwable, KeyEvent] { cb =>
          attempt(frame.addKeyListener(new KeyAdapter {
            override def keyPressed(e: KeyEvent): Unit =
              cb(succeed(Chunk.single(e)))
            override def keyReleased(e: KeyEvent): Unit =
              cb(succeed(Chunk.single(e)))
          }))
        }
        .groupByKey(_.getKeyCode)((k, s) => s.mapAccumZIO(Option.empty[Promise[Nothing, Unit]]) {
          case (Some(p), e) if e.getID == KeyEvent.KEY_RELEASED => p.succeed(()).map(_ => (None, None))
          case (None, e) if e.getID == KeyEvent.KEY_PRESSED => Promise.make[Nothing, Unit].map(p => (Some(p), Some(p)))
          case (s, _) => succeed((s, None))
        })
        .collect:
          case Some(p) => p

      (presses, releases) <- ZStream.asyncScoped[Any, Throwable, KeyEvent] { cb =>
          attempt(frame.addKeyListener(new KeyAdapter {
            override def keyPressed(e: KeyEvent): Unit = cb(succeed(Chunk.single(e)))
            override def keyReleased(e: KeyEvent): Unit = cb(succeed(Chunk.single(e)))
          })) *> debug("created key listener")
        }
        .changesWith((e1, e2) => e1.getKeyCode == e2.getKeyCode && e1.getID == e2.getID)
        .partition(_.getID == KeyEvent.KEY_PRESSED)

    yield new Service:
      override def activate: UIO[Unit] = succeed(frame.setVisible(true))
      override def deactivate: UIO[Unit] = succeed(frame.setVisible(false))
      override def keyPress: ZStream[Any, Throwable, KeyEvent] = presses
      override def keyRelease: ZStream[Any, Throwable, KeyEvent] = releases
      override def keys: ZStream[Any, Throwable, (RuntimeFlags, Promise[Nothing, Unit])] = ???
