package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class CovariantEqualsCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  check(
    new CovariantEqualsChecker,
    "CovariantEqualsChecker testClassOK",
    """
      |package foobar
      |
      |class OK {
      |  def equals(o: java.lang.Object): Boolean = false
      |  def equals(o: java.lang.Integer): Boolean = false
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class OK {
      |  def equals(o: java.lang.Object): Boolean = false
      |  def equals(o: java.lang.Integer): Boolean = false
      |}
      |""".stripMargin
  )

  check(
    new CovariantEqualsChecker,
    "CovariantEqualsChecker testClassCovariantEqualsNoObjectKO",
    """
      |package foobar
      |
      |class CovariantEqualsNoObjectKO {
      |  def equals(o: java.lang.Integer): Boolean = false
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class CovariantEqualsNoObjectKO/* scalafix:ok */ {
      |  def equals(o: java.lang.Integer): Boolean = false
      |}
      |""".stripMargin
  )

  check(
    new CovariantEqualsChecker,
    "CovariantEqualsChecker testObjectOK",
    """
      |package foobar
      |
      |object OK {
      |  def equals(o: java.lang.Object): Boolean = false
      |  def equals(o: java.lang.Integer): Boolean = false
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |object OK {
      |  def equals(o: java.lang.Object): Boolean = false
      |  def equals(o: java.lang.Integer): Boolean = false
      |}
      |""".stripMargin
  )

  check(
    new CovariantEqualsChecker,
    "CovariantEqualsChecker testObjectCovariantEqualsNoObjectKO",
    """
      |package foobar
      |
      |object CovariantEqualsNoObjectKO {
      |  def equals(o: java.lang.Integer): Boolean = false
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |object CovariantEqualsNoObjectKO/* scalafix:ok */ {
      |  def equals(o: java.lang.Integer): Boolean = false
      |}
      |""".stripMargin
  )
}
