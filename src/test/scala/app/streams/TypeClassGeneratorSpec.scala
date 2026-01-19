package app.streams

import zio.*
import ZIO.*
import zio.stream.ZStream
import zio.test.*

import scala.compiletime.{erasedValue, summonInline}
import scala.deriving.Mirror

// https://habr.com/ru/articles/536372/
trait Inverter[T]:
  def invert(v: T): T

object Inverter:
  given Inverter[String] = _.reverse
  given Inverter[Int] = -_
  given Inverter[Boolean] = !_

  inline def derived[T](using m: Mirror.Of[T]): Inverter[T] =
    val elemInstances = summonAll[m.MirroredElemTypes]
    inline m match
      case p: Mirror.ProductOf[T] => productInverter[T](p, elemInstances)
      case s: Mirror.SumOf[T] => ???

  inline def summonAll[T <: Tuple]: List[Inverter[?]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts) => summonInline[Inverter[t]] :: summonAll[ts]

  def productInverter[T](p: Mirror.ProductOf[T], elems: List[Inverter[?]]): Inverter[T] =
    (v: T) =>
      val o = v.asInstanceOf[Product].productIterator
      val n = o
        .zip(elems)
        .map:
          case (value, inverter) => inverter.asInstanceOf[Inverter[Any]].invert(value)
        .map(_.asInstanceOf[AnyRef])
        .toArray

      p.fromProduct(Tuple.fromArray(n))

case class Sample(int: Int, str: String, bool: Boolean) derives Inverter

object TypeClassGeneratorSpec extends ZIOSpecDefault:
  given Inverter[Sample] = Inverter.derived[Sample]

  inline given[T] (using m: Mirror.Of[T]): Inverter[T] = Inverter.derived[T]

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("actors spec")(
    test("should work"):
      for
        _ <- unit
      yield assertTrue(Sample(1, "2345", true) == summon[Inverter[Sample]].invert(Sample(-1, "5432", false)))
  )
