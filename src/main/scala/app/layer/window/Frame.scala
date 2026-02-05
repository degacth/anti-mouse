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

  private val jFrame = JFrame() // Due to "A fatal error has been detected by the Java Runtime Environment"

  def live: ZLayer[Any, Throwable, Service] = ZLayer.scoped:
    val normalColor = Color.MAGENTA
    val highlightColor = Color.RED
    for
      frame <- acquireRelease(attemptBlocking(jFrame) <* debug("frame created")): frm =>
        succeed(frm.dispose()) <* debug("frame disposed")

      _ <- succeed:
        import frame.*
        import buildinfo.BuildInfo

        setIconImage(Toolkit.getDefaultToolkit.getImage(this.getClass.getClassLoader.getResource("icon.png")))
        setFocusable(true)
        setTitle(BuildInfo.name)
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
        setUndecorated(true)
        setAlwaysOnTop(true)
        val size = 8
        setBounds(Rectangle(size, size, size, size))
        getContentPane.setBackground(normalColor)
        setVisible(false)

    yield new Service:
      override def addKeyListener(l: KeyListener): Unit = frame.addKeyListener(l)
      override def activate: UIO[Unit] =
        succeed(frame.setVisible(true))
          *> succeed(frame.toFront())
          *> succeed(frame.requestFocus())
      override def deactivate: UIO[Unit] = succeed(frame.setVisible(false))
      override def highlight: UIO[Unit] = succeed(frame.getContentPane.setBackground(highlightColor))
      override def offHighlight: UIO[Unit] = succeed(frame.getContentPane.setBackground(normalColor))
