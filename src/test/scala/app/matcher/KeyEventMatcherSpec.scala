package app.matcher

import zio.*
import zio.ZIO.*
import zio.test.*

import java.awt.Button
import java.awt.event.{InputEvent, KeyEvent}

object KeyEventMatcherSpec extends ZIOSpecDefault:
  import KeyEventMatcher.*

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("window specs")(
    test("try it only"):
      val e = new KeyEvent(Button("foo"), KeyEvent.KEY_PRESSED, 0, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK, KeyEvent.VK_J, 'j')

      val actual = List(
        e match
          case Press |> KeyEvent.VK_V => -2
          case Press |> KeyEvent.VK_J => 1
          case _ => 0
        ,
        e match
          case Press |> (KeyEvent.VK_V | KeyEvent.VK_K) => 2
          case Press |> (KeyEvent.VK_V | KeyEvent.VK_J) => 1
        ,
        e match
          case (Press |> (KeyEvent.VK_V | KeyEvent.VK_J)) <| Ctrl => -2
          case (Press |> (KeyEvent.VK_V | KeyEvent.VK_K)) <| Shift => -1
          case (Press |> (KeyEvent.VK_V | KeyEvent.VK_J)) <| Shift => 1
        ,
        e match
          case (Press |> (KeyEvent.VK_V | KeyEvent.VK_J)) <| Alt ++ (Shift | Ctrl) => 1
        ,
        e match
          case (Press |> KeyEvent.VK_J) <| Alt ++ Ctrl => -1
          case _ => 1
        ,
        e match
          case (Release |> KeyEvent.VK_J) <| Shift => -1
          case _ => 1
        ,
        e match
          case (Release |> AnyOne) <| Shift => -1
          case (Press |> AnyOne) <| Alt => 1
      )

      for
        _ <- unit
      yield assertTrue(actual == List.fill(actual.size)(1))
    ,
  )
