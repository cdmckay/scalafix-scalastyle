package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class NumberOfMethodsInTypeCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  check(
    new NumberOfMethodsInTypeChecker(
      NumberOfMethodsInTypeCheckerConfig(maxMethods = 4)
    ),
    "NumberOfMethodsInTypeChecker testOK",
    """
      |package foobar
      |
      |class F1() {
      |  def method1() = 1
      |  def method2() = 1
      |  def method3() = 1
      |  def method4() = 1
      |  def method5() = 1
      |}
      |
      |class F2() {
      |  def method1() = {
      |    def foobar() = 6
      |    5
      |  }
      |  def method2() = 1
      |  def method3() = 1
      |  def method4() = 1
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class F1/* scalafix:ok */() {
      |  def method1() = 1
      |  def method2() = 1
      |  def method3() = 1
      |  def method4() = 1
      |  def method5() = 1
      |}
      |
      |class F2() {
      |  def method1() = {
      |    def foobar() = 6
      |    5
      |  }
      |  def method2() = 1
      |  def method3() = 1
      |  def method4() = 1
      |}
      |""".stripMargin
  )
}
