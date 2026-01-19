package app.streams

import zio.*
import ZIO.*
import zio.stream.ZStream
import zio.test.*

type MiddlewareHandler[In, Out, R, E] = (In, ZIO[R, E, Out]) => ZIO[R, E, Out]
case class Middleware[In, Out, R, E](handlers: Chunk[MiddlewareHandler[In, Out, R, E]] = Chunk.empty)

object Middleware:
  def use[In, Out, R, E]: PartialFunction[MiddlewareHandler[In, Out, R, E], Middleware[In, Out, R, E]] = ???

object ZIOMiddleware extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment & Scope, Any] = suite("middleware")(
    test("should work"):
      val middleware = Middleware
        .use[Int, String, Any, Nothing]:
          case (n, _) if n % 3 == 0 => succeed(n * 1000).map(_.toString)
        .use:
          case (n, next) if n % 2 == 0 => succeed(-n).flatMap(next)
        .use:
          case (n, _) => succeed(_.toString)

      for
        actual <- ZStream
          .iterate(0)(_ + 1)
          .take(6)
          .mapZIO(middleware)
          .runCollect
      yield assertTrue(actual == Chunk(0, 1, 2, 3, 4, 5))
  )
