package app.streams

import app.domain.Key
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
    def activate: UIO[Unit]
    def deactivate: UIO[Unit]
    def show: UIO[Unit]
    def hide: UIO[Unit]
    def listen: KeyListener => UIO[Unit]
    def commands: UStream[Activator.Message]

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

        activator <- Queue.unbounded[Activator.Message]

      yield new Service:
        override def show: UIO[Unit] = succeed(frame.setVisible(true))
        override def hide: UIO[Unit] = succeed(frame.setVisible(false))
        override def listen: KeyListener => UIO[Unit] = l => succeed(frame.addKeyListener(l))
        override val commands: UStream[Activator.Message] = ZStream.fromQueue(activator)
        override def activate: UIO[Unit] = activator.offer(Activator.Message.Activate).unit
        override def deactivate: UIO[Unit] = activator.offer(Activator.Message.Deactivate).unit

  val activator: ZPipeline[Service, Throwable, Activator.Message, Activator.Message] =
    def hide: Activator.Message => URIO[Service, Activator.Message] = m =>
      serviceWithZIO[Service](_.hide.map(_ => m))

    def show: Activator.Message => URIO[Service, Activator.Message] = m =>
      serviceWithZIO[Service](_.show.map(_ => m))

    ZPipeline.mapAccumZIO(FrameState.Hidden):
      case (FrameState.Hidden, m@Activator.Message.Toggle) => show(m).map((FrameState.Shown, _))
      case (FrameState.Shown, m@Activator.Message.Toggle) => hide(m).map((FrameState.Hidden, _))
      case (FrameState.Hidden, m@Activator.Message.Activate) => show(m).map((FrameState.Shown, _))
      case (FrameState.Shown, m@Activator.Message.Deactivate) => hide(m).map((FrameState.Hidden, _))
      case m => succeed(m)

  val keyboardStream: ZStream[Service, Throwable, Key] =
    ZStream.asyncZIO[Service, Throwable, Key]: cb =>
      serviceWithZIO[Service](_.listen(new KeyAdapter:
        override def keyPressed(e: KeyEvent): Unit = cb.single(Key.pressed(e.getKeyCode))
        override def keyReleased(e: KeyEvent): Unit = cb.single(Key.released(e.getKeyCode))
      ))
