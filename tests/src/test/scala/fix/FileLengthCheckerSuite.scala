package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class FileLengthCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  private val rule =
    new FileLengthChecker(FileLengthCheckerConfig(maxFileLength = 5))

  check(
    rule,
    "FileLengthChecker testZero",
    """
      |package foobar
      |
      |  object Foobar {
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |  object Foobar {
      |}
      |""".stripMargin
  )

  check(
    rule,
    "FileLengthChecker testOne",
    """
      |package foobar
      |
      |  object Foobar {
      |}
      |  object Barbar {
      |}
      |""".stripMargin,
    """
      |/* scalafix:ok */package foobar
      |
      |  object Foobar {
      |}
      |  object Barbar {
      |}
      |""".stripMargin
  )
}
