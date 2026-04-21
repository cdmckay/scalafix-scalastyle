package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class NumberOfTypesCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  check(
    new NumberOfTypesChecker(NumberOfTypesCheckerConfig(maxTypes = 6)),
    "NumberOfTypesChecker testOK",
    """
      |package foobar
      |
      |case class F1()
      |case class F2()
      |case class F3()
      |case class F4()
      |case class F5()
      |case class F6()
      |""".stripMargin,
    """
      |package foobar
      |
      |case class F1()
      |case class F2()
      |case class F3()
      |case class F4()
      |case class F5()
      |case class F6()
      |""".stripMargin
  )

  check(
    new NumberOfTypesChecker(NumberOfTypesCheckerConfig(maxTypes = 5)),
    "NumberOfTypesChecker testKO",
    """
      |package foobar
      |
      |case class F1()
      |case class F2()
      |case class F3()
      |case class F4()
      |case class F5()
      |case class F6()
      |""".stripMargin,
    """
      |/* scalafix:ok */package foobar
      |
      |case class F1()
      |case class F2()
      |case class F3()
      |case class F4()
      |case class F5()
      |case class F6()
      |""".stripMargin
  )

  check(
    new NumberOfTypesChecker(NumberOfTypesCheckerConfig(maxTypes = 6)),
    "NumberOfTypesChecker testInnerClasses",
    """
      |package foobar
      |
      |case class F1()
      |case class F2()
      |case class F3()
      |case class F4()
      |class F5() {
      |  class Foobar {
      |  }
      |}
      |case class F6()
      |""".stripMargin,
    """
      |/* scalafix:ok */package foobar
      |
      |case class F1()
      |case class F2()
      |case class F3()
      |case class F4()
      |class F5() {
      |  class Foobar {
      |  }
      |}
      |case class F6()
      |""".stripMargin
  )
}
