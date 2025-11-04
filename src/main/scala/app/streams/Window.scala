package app.streams

import zio.*
import zio.stream.*

import java.awt.event.{KeyAdapter, KeyEvent, KeyListener}
import java.awt.{Color, Rectangle}
import javax.swing.{JFrame, WindowConstants}

object Window:
  import ZIO.*

  private enum FrameState:
    case Shown, Hidden

  trait Service:
    def show: UIO[Unit]
    def hide: UIO[Unit]
    def listen: KeyListener => UIO[Unit]

  def frame: TaskLayer[Service] =
    ZLayer.scoped:
      for
        frame <- acquireRelease(succeed(JFrame()) <* debug("frame created")): f =>
          succeed(f.dispose()) <* debug("frame disposed")

        _ <- succeed:
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

      yield new Service:
        override def show: UIO[Unit] = succeed(frame.setVisible(true))
        override def hide: UIO[Unit] = succeed(frame.setVisible(false))
        override def listen: KeyListener => UIO[Unit] = l => succeed(frame.addKeyListener(l))

  val activator: ZPipeline[Service, Throwable, Activator.Message, Unit] = ZPipeline.mapAccumZIO(FrameState.Hidden):
    case (FrameState.Hidden, _) => service[Service].flatMap(_.show.map(_ => (FrameState.Shown, ())))
    case (FrameState.Shown, _) => service[Service].flatMap(_.hide.map(_ => (FrameState.Hidden, ())))

  val keyboardStream: ZStream[Service, Throwable, Any] =
    ZStream.asyncZIO[Service, Throwable, Any]: cb =>
      serviceWithZIO[Service](_.listen(new KeyAdapter {
        override def keyPressed(e: KeyEvent): Unit = cb.single(e.getKeyCode)
      }))

    .tap(debug(_))

