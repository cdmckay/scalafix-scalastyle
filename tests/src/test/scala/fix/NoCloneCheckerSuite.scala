package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class NoCloneCheckerSuite extends AbstractSyntacticRuleSuite with AnyFunSuiteLike {
  check(
    new NoCloneChecker,
    "NoCloneChecker testClassOK",
    """
      |package foobar
      |
      |class OK {
      |  def clone(o: java.lang.Integer): Any = null
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class OK {
      |  def clone(o: java.lang.Integer): Any = null
      |}
      |""".stripMargin
  )

  check(
    new NoCloneChecker,
    "NoCloneChecker testClassCovariantEqualsNoObjectKO",
    """
      |package foobar
      |
      |class CloneKO {
      |  def clone(): Any = null
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class CloneKO {
      |  def clone/* scalafix:ok */(): Any = null
      |}
      |""".stripMargin
  )

  check(
    new NoCloneChecker,
    "NoCloneChecker testObjectOK",
    """
      |package foobar
      |
      |object OK {
      |  def clone(o: java.lang.Integer): Any = null
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |object OK {
      |  def clone(o: java.lang.Integer): Any = null
      |}
      |""".stripMargin
  )

  check(
    new NoCloneChecker,
    "NoCloneChecker testObjectCovariantEqualsNoObjectKO",
    """
      |package foobar
      |
      |object CloneKO {
      |  def clone(): Any = null
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |object CloneKO {
      |  def clone/* scalafix:ok */(): Any = null
      |}
      |""".stripMargin
  )
}
