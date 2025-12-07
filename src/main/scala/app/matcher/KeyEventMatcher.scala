package app.matcher

import java.awt.event.KeyEvent

object KeyEventMatcher:
  object |> :
    def unapply(e: KeyEvent) = IdEventKeyMatcher(e)

  object <| :
    def unapply(e: KeyEvent) =
      ModEventKeyMatcher(e)

  object ++ :
    def unapply(e: KeyEvent) = ModCombined(e)

  class IdEventKeyMatcher(e: KeyEvent) extends NotImplementedProduct:
    def _1: Int = e.getID
    def _2: Int | AnyOne.type = e.getKeyCode

  class ModEventKeyMatcher(e: KeyEvent) extends NotImplementedProduct:
    def _1 = e
    def _2 = e

  trait Modifier:
    def isModified: KeyEvent => Boolean
    override def equals(obj: Any): Boolean = obj match
      case e: KeyEvent => isModified(e)
      case _ => false

  case object Shift extends Modifier:
    override val isModified: KeyEvent => Boolean = _.isShiftDown

  case object Alt extends Modifier:
    override val isModified: KeyEvent => Boolean = _.isAltDown

  case object Ctrl extends Modifier:
    override val isModified: KeyEvent => Boolean = _.isControlDown

  val Press: Int = KeyEvent.KEY_PRESSED
  val Release: Int = KeyEvent.KEY_RELEASED

  object AnyOne:
    override def equals(obj: Any): Boolean =
      obj match
        case _: Int => true
        case _ => super.equals(obj)

  class ModCombined(e: KeyEvent) extends NotImplementedProduct:
    def _1 = e
    def _2 = e

  protected trait NotImplementedProduct extends Product:
    override def canEqual(that: Any): Boolean = ???
    override def productArity: Int = ???
    override def productElement(n: Int): Any = ???

