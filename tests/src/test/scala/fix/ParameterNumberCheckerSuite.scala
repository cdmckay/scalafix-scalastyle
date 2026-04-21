package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class ParameterNumberCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  private val rule =
    new ParameterNumberChecker(ParameterNumberCheckerConfig(maxParameters = 8))

  check(
    rule,
    "ParameterNumberChecker testOK",
    """
      |package foobar
      |
      |class OK {
      |  def method(i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int, i8: Int): Int = 45
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class OK {
      |  def method(i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int, i8: Int): Int = 45
      |}
      |""".stripMargin
  )

  check(
    rule,
    "ParameterNumberChecker testKO",
    """
      |package foobar
      |
      |class OK {
      |  def method(i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int, i8: Int, i9: Int): Int = 45
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class OK {
      |  def method/* scalafix:ok */(i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int, i8: Int, i9: Int): Int = 45
      |}
      |""".stripMargin
  )

  check(
    rule,
    "ParameterNumberChecker testOuterKOInnerKO",
    """
      |package foobar
      |
      |class Outer {
      |  object Inner {
      |    def method(i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int, i8: Int, i9: Int): Int = 45
      |  }
      |
      |  class Inner {
      |    def method(i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int, i8: Int, i9: Int): Int = 45
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class Outer {
      |  object Inner {
      |    def method/* scalafix:ok */(i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int, i8: Int, i9: Int): Int = 45
      |  }
      |
      |  class Inner {
      |    def method/* scalafix:ok */(i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int, i8: Int, i9: Int): Int = 45
      |  }
      |}
      |""".stripMargin
  )
}
