package app.layer.force.transformer

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent

import java.awt.event.KeyEvent
import javax.swing.JButton

object ForsedKey:
  private val stubComponent = JButton()

  def apply(e: NativeKeyEvent): KeyEvent =
    KeyEvent(
      stubComponent,
      KeyCodeMapper.fromNativeKey(e.getID),
      e.getWhen,
      KeyCodeMapper.modifiers(e),
      KeyCodeMapper.fromNativeKey(e.getKeyCode),
      e.getKeyChar
    )
