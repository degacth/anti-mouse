package app.layer.window

import zio.*
import zio.ZIO.*

import java.awt.*
import javax.swing.*

object Frame:
  trait Service:
    def addKeyListener(l: java.awt.event.KeyListener): Unit = ()
    def activate: UIO[Unit] = unit
    def deactivate: UIO[Unit] = unit

  def live: ULayer[Service] = ZLayer.scoped:
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

    yield new Service:
      override def addKeyListener(l: java.awt.event.KeyListener): Unit = jFrame.addKeyListener(l)
      override def activate: UIO[Unit] = succeed(jFrame.setVisible(true))
      override def deactivate: UIO[Unit] = succeed(jFrame.setVisible(false))