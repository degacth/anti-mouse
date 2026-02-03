package app.layer.window

import zio.*
import zio.ZIO.*

import java.awt.*
import java.awt.event.KeyListener
import javax.swing.*

object Frame:
  trait Service:
    def addKeyListener(l: KeyListener): Unit
    def activate: UIO[Unit] = unit
    def deactivate: UIO[Unit] = unit
    def highlight: UIO[Unit]
    def offHighlight: UIO[Unit]

  def live: ZLayer[Any, Throwable, Service] = ZLayer.scoped:
    val normalColor = Color.MAGENTA
    val highlightColor = Color.RED
    for
      jFrame <- acquireRelease(attemptBlockingIO(JFrame()) <* debug("frame created")): frm =>
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
        getContentPane.setBackground(normalColor)
        setVisible(false)

    yield new Service:
      override def addKeyListener(l: KeyListener): Unit = jFrame.addKeyListener(l)
      override def activate: UIO[Unit] =
        succeed(jFrame.setVisible(true))
          *> succeed(jFrame.toFront())
          *> succeed(jFrame.requestFocus())
      override def deactivate: UIO[Unit] = succeed(jFrame.setVisible(false))
      override def highlight: UIO[Unit] = succeed(jFrame.getContentPane.setBackground(highlightColor))
      override def offHighlight: UIO[Unit] = succeed(jFrame.getContentPane.setBackground(normalColor))
