package stubs

import zio.*
import app.layer.Screen

object ScreenStub:
  def display: ULayer[Screen.Display] = ZLayer.succeed(new Screen.Display:
    private val resolution = 1000
    override def size: Task[(Double, Double)] = ZIO.succeed((2 * resolution, resolution))
  )
  
  def live: ULayer[Screen.Service] = ZLayer.succeed(new Screen.Service:
    override def screenPart(dim: (Int, Int), point: (Int, Int)): UIO[(Int, Int)] = ZIO.succeed((0, 0))
  )
