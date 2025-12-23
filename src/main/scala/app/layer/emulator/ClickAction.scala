package app.layer.emulator

import app.domain.CursorAction
import zio.*
import ZIO.*

import java.awt.event.KeyEvent

object ClickAction:
  trait Service:
    import KeyEvent.*
    
    val has: KeyEvent => Boolean =
      case e if e.getKeyCode == VK_ENTER => true
      case _ => false
      
    val click: CursorAction = CursorAction.Click
    
  val live: ZLayer[Any, Throwable, Service] = ZLayer.succeed:
    new Service {}
