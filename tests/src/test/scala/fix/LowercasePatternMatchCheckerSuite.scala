package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class LowercasePatternMatchCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  check(
    new LowercasePatternMatchChecker,
    "LowercasePatternMatchChecker testOK",
    """package foobar
      |
      |class F1 {
      |  val lc = "foobar"
      |
      |  def fn(a: Any) = a match {
      |    case "barbar" => "barbar"
      |    case s: Int => "int"
      |    case List(x, y) => "list"
      |    case `lc` => "lc"
      |  }
      |}
      |""".stripMargin,
    """package foobar
      |
      |class F1 {
      |  val lc = "foobar"
      |
      |  def fn(a: Any) = a match {
      |    case "barbar" => "barbar"
      |    case s: Int => "int"
      |    case List(x, y) => "list"
      |    case `lc` => "lc"
      |  }
      |}
      |""".stripMargin
  )

  check(
    new LowercasePatternMatchChecker,
    "LowercasePatternMatchChecker testKO",
    """package foobar
      |
      |class F1 {
      |  val lc = "foobar"
      |
      |  def fn(a: Any) = a match {
      |    case "barbar" => "barbar"
      |    case s: Int => "int"
      |    case List(x, y) => "list"
      |    case lc => "lc"
      |  }
      |}
      |""".stripMargin,
    """package foobar
      |
      |class F1 {
      |  val lc = "foobar"
      |
      |  def fn(a: Any) = a match {
      |    case "barbar" => "barbar"
      |    case s: Int => "int"
      |    case List(x, y) => "list"
      |    case lc/* scalafix:ok */ => "lc"
      |  }
      |}
      |""".stripMargin
  )
}
