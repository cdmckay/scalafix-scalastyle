package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class ObjectNamesCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  check(
    new ObjectNamesChecker,
    "ObjectNamesChecker testZero",
    """
      |package foobar
      |
      |object Foobar {
      |  val foo = 1
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |object Foobar {
      |  val foo = 1
      |}
      |""".stripMargin
  )

  check(
    new ObjectNamesChecker,
    "ObjectNamesChecker testOne",
    """
      |package foobar
      |
      |object foobar {
      |  object barbar {
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |object foobar/* scalafix:ok */ {
      |  object barbar/* scalafix:ok */ {
      |  }
      |}
      |""".stripMargin
  )

  check(
    new ObjectNamesChecker,
    "ObjectNamesChecker testPackageObject",
    """
      |package foobar
      |
      |package object foobar {
      |  object barbar {
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |package object foobar {
      |  object barbar/* scalafix:ok */ {
      |  }
      |}
      |""".stripMargin
  )
}
