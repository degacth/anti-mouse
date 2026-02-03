package app.layer.activator.transformer

import com.github.kwhat.jnativehook.NativeInputEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent

import java.awt.event.{InputEvent, KeyEvent}

object KeyCodeMapper {
  val nativeToKey: Map[Int, Int] = Map(
    // Ids
    NativeKeyEvent.NATIVE_KEY_PRESSED -> KeyEvent.KEY_PRESSED,
    NativeKeyEvent.NATIVE_KEY_RELEASED -> KeyEvent.KEY_RELEASED,

    // Escape key
    NativeKeyEvent.VC_ESCAPE -> KeyEvent.VK_ESCAPE,

    // Function keys F1-F24
    NativeKeyEvent.VC_F1 -> KeyEvent.VK_F1,
    NativeKeyEvent.VC_F2 -> KeyEvent.VK_F2,
    NativeKeyEvent.VC_F3 -> KeyEvent.VK_F3,
    NativeKeyEvent.VC_F4 -> KeyEvent.VK_F4,
    NativeKeyEvent.VC_F5 -> KeyEvent.VK_F5,
    NativeKeyEvent.VC_F6 -> KeyEvent.VK_F6,
    NativeKeyEvent.VC_F7 -> KeyEvent.VK_F7,
    NativeKeyEvent.VC_F8 -> KeyEvent.VK_F8,
    NativeKeyEvent.VC_F9 -> KeyEvent.VK_F9,
    NativeKeyEvent.VC_F10 -> KeyEvent.VK_F10,
    NativeKeyEvent.VC_F11 -> KeyEvent.VK_F11,
    NativeKeyEvent.VC_F12 -> KeyEvent.VK_F12,
    NativeKeyEvent.VC_F13 -> KeyEvent.VK_F13,
    NativeKeyEvent.VC_F14 -> KeyEvent.VK_F14,
    NativeKeyEvent.VC_F15 -> KeyEvent.VK_F15,
    NativeKeyEvent.VC_F16 -> KeyEvent.VK_F16,
    NativeKeyEvent.VC_F17 -> KeyEvent.VK_F17,
    NativeKeyEvent.VC_F18 -> KeyEvent.VK_F18,
    NativeKeyEvent.VC_F19 -> KeyEvent.VK_F19,
    NativeKeyEvent.VC_F20 -> KeyEvent.VK_F20,
    NativeKeyEvent.VC_F21 -> KeyEvent.VK_F21,
    NativeKeyEvent.VC_F22 -> KeyEvent.VK_F22,
    NativeKeyEvent.VC_F23 -> KeyEvent.VK_F23,
    NativeKeyEvent.VC_F24 -> KeyEvent.VK_F24,

    // Number keys 0-9
    NativeKeyEvent.VC_1 -> KeyEvent.VK_1,
    NativeKeyEvent.VC_2 -> KeyEvent.VK_2,
    NativeKeyEvent.VC_3 -> KeyEvent.VK_3,
    NativeKeyEvent.VC_4 -> KeyEvent.VK_4,
    NativeKeyEvent.VC_5 -> KeyEvent.VK_5,
    NativeKeyEvent.VC_6 -> KeyEvent.VK_6,
    NativeKeyEvent.VC_7 -> KeyEvent.VK_7,
    NativeKeyEvent.VC_8 -> KeyEvent.VK_8,
    NativeKeyEvent.VC_9 -> KeyEvent.VK_9,
    NativeKeyEvent.VC_0 -> KeyEvent.VK_0,

    // Letter keys A-Z
    NativeKeyEvent.VC_A -> KeyEvent.VK_A,
    NativeKeyEvent.VC_B -> KeyEvent.VK_B,
    NativeKeyEvent.VC_C -> KeyEvent.VK_C,
    NativeKeyEvent.VC_D -> KeyEvent.VK_D,
    NativeKeyEvent.VC_E -> KeyEvent.VK_E,
    NativeKeyEvent.VC_F -> KeyEvent.VK_F,
    NativeKeyEvent.VC_G -> KeyEvent.VK_G,
    NativeKeyEvent.VC_H -> KeyEvent.VK_H,
    NativeKeyEvent.VC_I -> KeyEvent.VK_I,
    NativeKeyEvent.VC_J -> KeyEvent.VK_J,
    NativeKeyEvent.VC_K -> KeyEvent.VK_K,
    NativeKeyEvent.VC_L -> KeyEvent.VK_L,
    NativeKeyEvent.VC_M -> KeyEvent.VK_M,
    NativeKeyEvent.VC_N -> KeyEvent.VK_N,
    NativeKeyEvent.VC_O -> KeyEvent.VK_O,
    NativeKeyEvent.VC_P -> KeyEvent.VK_P,
    NativeKeyEvent.VC_Q -> KeyEvent.VK_Q,
    NativeKeyEvent.VC_R -> KeyEvent.VK_R,
    NativeKeyEvent.VC_S -> KeyEvent.VK_S,
    NativeKeyEvent.VC_T -> KeyEvent.VK_T,
    NativeKeyEvent.VC_U -> KeyEvent.VK_U,
    NativeKeyEvent.VC_V -> KeyEvent.VK_V,
    NativeKeyEvent.VC_W -> KeyEvent.VK_W,
    NativeKeyEvent.VC_X -> KeyEvent.VK_X,
    NativeKeyEvent.VC_Y -> KeyEvent.VK_Y,
    NativeKeyEvent.VC_Z -> KeyEvent.VK_Z,

    // Special character keys
    NativeKeyEvent.VC_BACKQUOTE -> KeyEvent.VK_BACK_QUOTE,
    NativeKeyEvent.VC_MINUS -> KeyEvent.VK_MINUS,
    NativeKeyEvent.VC_EQUALS -> KeyEvent.VK_EQUALS,
    NativeKeyEvent.VC_BACKSPACE -> KeyEvent.VK_BACK_SPACE,
    NativeKeyEvent.VC_TAB -> KeyEvent.VK_TAB,
    NativeKeyEvent.VC_CAPS_LOCK -> KeyEvent.VK_CAPS_LOCK,
    NativeKeyEvent.VC_OPEN_BRACKET -> KeyEvent.VK_OPEN_BRACKET,
    NativeKeyEvent.VC_CLOSE_BRACKET -> KeyEvent.VK_CLOSE_BRACKET,
    NativeKeyEvent.VC_BACK_SLASH -> KeyEvent.VK_BACK_SLASH,
    NativeKeyEvent.VC_SEMICOLON -> KeyEvent.VK_SEMICOLON,
    NativeKeyEvent.VC_QUOTE -> KeyEvent.VK_QUOTE,
    NativeKeyEvent.VC_ENTER -> KeyEvent.VK_ENTER,
    NativeKeyEvent.VC_COMMA -> KeyEvent.VK_COMMA,
    NativeKeyEvent.VC_PERIOD -> KeyEvent.VK_PERIOD,
    NativeKeyEvent.VC_SLASH -> KeyEvent.VK_SLASH,
    NativeKeyEvent.VC_SPACE -> KeyEvent.VK_SPACE,

    // Navigation keys
    NativeKeyEvent.VC_PRINTSCREEN -> KeyEvent.VK_PRINTSCREEN,
    NativeKeyEvent.VC_SCROLL_LOCK -> KeyEvent.VK_SCROLL_LOCK,
    NativeKeyEvent.VC_PAUSE -> KeyEvent.VK_PAUSE,
    NativeKeyEvent.VC_INSERT -> KeyEvent.VK_INSERT,
    NativeKeyEvent.VC_DELETE -> KeyEvent.VK_DELETE,
    NativeKeyEvent.VC_HOME -> KeyEvent.VK_HOME,
    NativeKeyEvent.VC_END -> KeyEvent.VK_END,
    NativeKeyEvent.VC_PAGE_UP -> KeyEvent.VK_PAGE_UP,
    NativeKeyEvent.VC_PAGE_DOWN -> KeyEvent.VK_PAGE_DOWN,
    NativeKeyEvent.VC_UP -> KeyEvent.VK_UP,
    NativeKeyEvent.VC_LEFT -> KeyEvent.VK_LEFT,
    NativeKeyEvent.VC_CLEAR -> KeyEvent.VK_CLEAR,
    NativeKeyEvent.VC_RIGHT -> KeyEvent.VK_RIGHT,
    NativeKeyEvent.VC_DOWN -> KeyEvent.VK_DOWN,

    // Numeric keypad
    NativeKeyEvent.VC_NUM_LOCK -> KeyEvent.VK_NUM_LOCK,
    NativeKeyEvent.VC_SEPARATOR -> KeyEvent.VK_SEPARATOR, // May need verification

    // Modifier keys
    NativeKeyEvent.VC_SHIFT -> KeyEvent.VK_SHIFT,
    NativeKeyEvent.VC_CONTROL -> KeyEvent.VK_CONTROL,
    NativeKeyEvent.VC_ALT -> KeyEvent.VK_ALT,
    NativeKeyEvent.VC_META -> KeyEvent.VK_META, // Could also map to VK_WINDOWS
    NativeKeyEvent.VC_CONTEXT_MENU -> KeyEvent.VK_CONTEXT_MENU,

    // Media keys
    NativeKeyEvent.VC_POWER -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_SLEEP -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_WAKE -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_MEDIA_PLAY -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_MEDIA_STOP -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_MEDIA_PREVIOUS -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_MEDIA_NEXT -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_MEDIA_SELECT -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_MEDIA_EJECT -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_VOLUME_MUTE -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_VOLUME_UP -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_VOLUME_DOWN -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_APP_MAIL -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_APP_CALCULATOR -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_APP_MUSIC -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_APP_PICTURES -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_BROWSER_SEARCH -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_BROWSER_HOME -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_BROWSER_BACK -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_BROWSER_FORWARD -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_BROWSER_STOP -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_BROWSER_REFRESH -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_BROWSER_FAVORITES -> KeyEvent.VK_UNDEFINED, // No direct equivalent

    // Japanese language keys
    NativeKeyEvent.VC_KATAKANA -> KeyEvent.VK_KATAKANA,
    NativeKeyEvent.VC_UNDERSCORE -> KeyEvent.VK_UNDERSCORE, // Assuming this maps to VK_UNDERSCORE
    NativeKeyEvent.VC_FURIGANA -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_KANJI -> KeyEvent.VK_KANJI,
    NativeKeyEvent.VC_HIRAGANA -> KeyEvent.VK_HIRAGANA,
    NativeKeyEvent.VC_YEN -> KeyEvent.VK_UNDEFINED, // No direct equivalent

    // Sun keyboards
    NativeKeyEvent.VC_SUN_HELP -> KeyEvent.VK_HELP,
    NativeKeyEvent.VC_SUN_STOP -> KeyEvent.VK_STOP,
    NativeKeyEvent.VC_SUN_PROPS -> KeyEvent.VK_PROPS,
    NativeKeyEvent.VC_SUN_FRONT -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_SUN_OPEN -> KeyEvent.VK_UNDEFINED, // No direct equivalent
    NativeKeyEvent.VC_SUN_FIND -> KeyEvent.VK_FIND,
    NativeKeyEvent.VC_SUN_AGAIN -> KeyEvent.VK_AGAIN,
    NativeKeyEvent.VC_SUN_UNDO -> KeyEvent.VK_UNDO,
    NativeKeyEvent.VC_SUN_COPY -> KeyEvent.VK_COPY,
    NativeKeyEvent.VC_SUN_INSERT -> KeyEvent.VK_INSERT, // Already mapped above
    NativeKeyEvent.VC_SUN_CUT -> KeyEvent.VK_CUT,

    // Undefined key
    NativeKeyEvent.VC_UNDEFINED -> KeyEvent.VK_UNDEFINED
  )

  def fromNativeKey(nativeKeyCode: Int): Int = nativeToKey.getOrElse(nativeKeyCode, KeyEvent.VK_UNDEFINED)

  def modifiers(e: NativeKeyEvent): Int =
    val mods = e.getModifiers
    (
      (mods & NativeInputEvent.ALT_MASK) != 0,
      (mods & NativeInputEvent.SHIFT_MASK) != 0,
    ) match
      case (true, true) => InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK
      case (true, _) => InputEvent.ALT_DOWN_MASK
      case (_, true) => InputEvent.SHIFT_DOWN_MASK
      case _ => 0
}
