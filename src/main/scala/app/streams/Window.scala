package app.streams

import app.domain.Key
import app.domain.WindowEvent
import zio.*
import zio.stream.*

import java.awt.event.{KeyAdapter, KeyEvent, KeyListener, WindowEvent => WE, WindowFocusListener}
import java.awt.{Color, Rectangle}
import javax.swing.{JFrame, WindowConstants}

object Window:
  import ZIO.*

  private enum FrameState:
    case Shown, Hidden

  enum Focus:
    case Lost, Gain

  trait Service:
    def activate: UIO[Unit]
    def deactivate: UIO[Unit]
    def show: UIO[Unit]
    def hide: UIO[Unit]
    def listen: KeyListener => UIO[Unit]
    def focus: WindowFocusListener => UIO[Unit]
    def commands: UStream[WindowEvent]

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

        activator <- Queue.unbounded[WindowEvent]

      yield new Service:
        override def show: UIO[Unit] = succeed(frame.setVisible(true))
        override def hide: UIO[Unit] = succeed(frame.setVisible(false))
        override def listen: KeyListener => UIO[Unit] = l => succeed(frame.addKeyListener(l))
        override val commands: UStream[WindowEvent] = ZStream.fromQueue(activator)
        override def activate: UIO[Unit] = activator.offer(WindowEvent.Activate).unit
        override def deactivate: UIO[Unit] = activator.offer(WindowEvent.Deactivate).unit
        override def focus: WindowFocusListener => UIO[Unit] = l => succeed(frame.addWindowFocusListener(l))

  val activator: ZPipeline[Service, Throwable, WindowEvent, WindowEvent] =
    def hide: WindowEvent => URIO[Service, WindowEvent] = m =>
      serviceWithZIO[Service](_.hide.map(_ => m))

    def show: WindowEvent => URIO[Service, WindowEvent] = m =>
      serviceWithZIO[Service](_.show.map(_ => m))

    ZPipeline.mapAccumZIO(FrameState.Hidden):
      case (FrameState.Hidden, m@WindowEvent.Toggle) => show(m).map((FrameState.Shown, _))
      case (FrameState.Shown, m@WindowEvent.Toggle) => hide(m).map((FrameState.Hidden, _))
      case (FrameState.Hidden, m@WindowEvent.Activate) => show(m).map((FrameState.Shown, _))
      case (FrameState.Shown, m@WindowEvent.Deactivate) => hide(m).map((FrameState.Hidden, _))
      case m => succeed(m)

  val keyboardStream: ZStream[Service, Throwable, Key] =
    ZStream.asyncZIO[Service, Throwable, Key]: cb =>
      serviceWithZIO[Service](_.listen(new KeyAdapter:
        override def keyPressed(e: KeyEvent): Unit = cb.single(Key.pressed(e.getKeyCode))
        override def keyReleased(e: KeyEvent): Unit = cb.single(Key.released(e.getKeyCode))
      ))

  val focusStream: ZStream[Service, Throwable, Focus] = ZStream.asyncZIO: cb =>
    serviceWithZIO[Service](_.focus(new WindowFocusListener {
      override def windowGainedFocus(e: WE): Unit = cb.single(Focus.Gain)
      override def windowLostFocus(e: WE): Unit = cb.single(Focus.Lost)
    }))
