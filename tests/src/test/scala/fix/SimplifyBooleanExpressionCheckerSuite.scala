package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class SimplifyBooleanExpressionCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  check(
    new SimplifyBooleanExpressionChecker,
    "SimplifyBooleanExpressionChecker testEquals",
    """
      |package foobar
      |
      |object Foobar {
      |  val b = true
      |  val foo01 = (b == true)
      |  val foo02 = (b != true)
      |  val foo03 = (b == false)
      |  val foo04 = (b != false)
      |}""".stripMargin,
    """
      |package foobar
      |
      |object Foobar {
      |  val b = true
      |  val foo01 = (b/* scalafix:ok */ == true)
      |  val foo02 = (b/* scalafix:ok */ != true)
      |  val foo03 = (b/* scalafix:ok */ == false)
      |  val foo04 = (b/* scalafix:ok */ != false)
      |}""".stripMargin
  )

  check(
    new SimplifyBooleanExpressionChecker,
    "SimplifyBooleanExpressionChecker testErrors",
    """
      |package foobar
      |
      |object Foobar {
      |  val b = true
      |  val foo01 = (b == true)
      |  val foo02 = !false
      |  val foo03 = !true
      |}""".stripMargin,
    """
      |package foobar
      |
      |object Foobar {
      |  val b = true
      |  val foo01 = (b/* scalafix:ok */ == true)
      |  val foo02 = !/* scalafix:ok */false
      |  val foo03 = !/* scalafix:ok */true
      |}""".stripMargin
  )

  check(
    new SimplifyBooleanExpressionChecker,
    "SimplifyBooleanExpressionChecker testErrors2",
    """
      |package foobar
      |
      |object Foobar {
      |  val b = true
      |  val foo04 = b && true
      |  val foo05 = true && b
      |  val foo06 = b && false
      |  val foo07 = false && b
      |}""".stripMargin,
    """
      |package foobar
      |
      |object Foobar {
      |  val b = true
      |  val foo04 = b/* scalafix:ok */ && true
      |  val foo05 = true/* scalafix:ok */ && b
      |  val foo06 = b/* scalafix:ok */ && false
      |  val foo07 = false/* scalafix:ok */ && b
      |}""".stripMargin
  )

  check(
    new SimplifyBooleanExpressionChecker,
    "SimplifyBooleanExpressionChecker testErrors3",
    """
      |package foobar
      |
      |object Foobar {
      |  val b = true
      |  val foo08 = b || true
      |  val foo09 = true || b
      |  val foo10 = b || false
      |  val foo11 = false || b
      |}""".stripMargin,
    """
      |package foobar
      |
      |object Foobar {
      |  val b = true
      |  val foo08 = b/* scalafix:ok */ || true
      |  val foo09 = true/* scalafix:ok */ || b
      |  val foo10 = b/* scalafix:ok */ || false
      |  val foo11 = false/* scalafix:ok */ || b
      |}""".stripMargin
  )

  check(
    new SimplifyBooleanExpressionChecker,
    "SimplifyBooleanExpressionChecker testOK",
    """
      |package foobar
      |
      |object Foobar {
      |  val b = true
      |  val foo12 = b && b // doesn't match
      |  val foo13 = (b && b) || b
      |  val foo14 = b && (true)
      |}""".stripMargin,
    """
      |package foobar
      |
      |object Foobar {
      |  val b = true
      |  val foo12 = b && b // doesn't match
      |  val foo13 = (b && b) || b
      |  val foo14 = b/* scalafix:ok */ && (true)
      |}""".stripMargin
  )
}
