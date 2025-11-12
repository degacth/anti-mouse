package app.domain

enum Key:
  case Pressed(override val code: Int)
  case Released(override val code: Int)

  def code: Int = this match
    case Pressed(code) => code
    case Released(code) => code

  override def toString: String = s"${this.getClass.getSimpleName}(${java.awt.event.KeyEvent.getKeyText(code)})"

object Key:
  val pressed: Int => Key = Pressed(_)
  val released: Int => Key = Released(_)
