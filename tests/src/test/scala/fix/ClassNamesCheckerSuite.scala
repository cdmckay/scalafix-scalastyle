package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class ClassNamesCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  check(
    new ClassNamesChecker,
    "ClassNamesChecker testZero",
    """
      |package foobar
      |
      |class Foobar {
      |  val foo = 1
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class Foobar {
      |  val foo = 1
      |}
      |""".stripMargin
  )

  check(
    new ClassNamesChecker,
    "ClassNamesChecker testOne",
    """
      |package foobar
      |
      |class foobar {
      |  class barbar {
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class foobar/* scalafix:ok */ {
      |  class barbar/* scalafix:ok */ {
      |  }
      |}
      |""".stripMargin
  )
}
