package app.layer

import utils.Specs
import zio.*
import zio.test.*

object ScreenSpec extends ZIOSpecDefault:
  import ZIO.*

  val screenSize = 1000d
  val tilesSize = 5

  val display: ULayer[Screen.Display] = ZLayer:
    succeed:
      new Screen.Display:
        override def size: Task[(Double, Double)] = succeed((screenSize * 2, screenSize))

  def spec = suite("screen spec")(
    test("should get screen part"):
      Specs.withFixtures(List(
        (((tilesSize, tilesSize), (0, 0)), (200, 100)),
        (((tilesSize, tilesSize), (1, 1)), (600, 300)),
        (((tilesSize, tilesSize), (4, 4)), (1800, 900)),
      )):
        case (dim, point) =>
          serviceWithZIO[Screen.Service](_.screenPart(dim, point))
  )
    .provide(
      Screen.live,
      display,
    )
