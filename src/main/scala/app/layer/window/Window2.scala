package app.layer.window

import app.layer.activator.Activator2
import zio.*
import zio.ZIO.*
import zio.stream.{Stream, UStream, ZStream}

import java.awt.event.{KeyAdapter, KeyEvent, WindowEvent, WindowFocusListener}
import java.awt.{Color, Rectangle}
import javax.swing.{JFrame, WindowConstants}

object Window2:
  import java.awt.event.KeyEvent

  trait Service:
    def keys: ZStream[Any, Throwable, KeyEvent]
    def toggleVisibility: Task[Unit]
    def windowActions: UStream[WindowAction]

  val live: RLayer[Activator2.Service, Service] = ZLayer.scoped:
    for
      frame <- acquireRelease(succeed(JFrame()) <* debug("frame created")): f =>
        succeed(f.dispose()) <* debug("frame disposed")

      _ <- attempt:
        import frame.*

        setFocusable(true)
        setTitle("AntiMouse")
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
        setUndecorated(true)
        setAlwaysOnTop(true)
        val size = 8
        setBounds(new Rectangle(size, size, size, size))
        getContentPane.setBackground(Color.MAGENTA)
        setVisible(false)

      actions = ZStream.asyncZIO[Any, Nothing, WindowAction]: cb =>
        succeed:
          frame.addWindowFocusListener:
            new WindowFocusListener {
              override def windowGainedFocus(e: WindowEvent): Unit = ()
              override def windowLostFocus(e: WindowEvent): Unit = cb.single(WindowAction.FocusLost)
            }

      shouldBeShown <- Ref.make(true)
    yield new Service {
      private val keysStream: Stream[Throwable, KeyEvent] = ZStream
        .asyncZIO[Any, Throwable, KeyEvent]: cb =>
          attempt:
            frame.addKeyListener:
              new KeyAdapter {
                override def keyPressed(e: KeyEvent): Unit = cb.single(e)
                override def keyReleased(e: KeyEvent): Unit = cb.single(e)
              }

      override val keys: Stream[Throwable, KeyEvent] = keysStream

      override def toggleVisibility: Task[Unit] = shouldBeShown
        .getAndUpdate(!_)
        .flatMap(v => attempt(frame.setVisible(v)))

      override def windowActions: UStream[WindowAction] = actions
    }
