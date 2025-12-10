package app.domain

enum Direction:
  case Up
  case Right
  case Down
  case Left

  def diff: Int = this match
    case Up | Left => -1
    case Down | Right => 1

enum CursorAction:
  case Absolute(x: Int, y: Int)
  case StartMove(dir: Direction)
  case StopMove(dir: Direction)
