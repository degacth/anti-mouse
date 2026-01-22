package app.layer.window

import zio.*
import ZIO.*
import java.awt.*
import javax.swing.*

object Window:
  trait Service:
    def activate: UIO[Unit]
    def deactivate: UIO[Unit]

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
        setBounds(new Rectangle(size, size, size, size))
        getContentPane.setBackground(Color.MAGENTA)
        setVisible(false)

    yield new Service:
      override def activate: UIO[Unit] = succeed(frame.setVisible(true))
      override def deactivate: UIO[Unit] = succeed(frame.setVisible(false))
