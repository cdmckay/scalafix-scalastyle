package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class StructuralTypeCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  check(
    new StructuralTypeChecker,
    "StructuralTypeChecker testKO",
    """
      |package foobar
      |
      |class Foobar {
      |  def string[T <: String](t: T) = {}
      |  def structuralType[T <: {def close(): Unit }](t: T) = {}
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class Foobar {
      |  def string[T <: String](t: T) = {}
      |  def structuralType[T <: {/* scalafix:ok */def close(): Unit }](t: T) = {}
      |}
      |""".stripMargin
  )
}
