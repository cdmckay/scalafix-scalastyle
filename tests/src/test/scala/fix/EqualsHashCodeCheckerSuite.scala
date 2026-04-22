package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class EqualsHashCodeCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  check(
    new EqualsHashCodeChecker,
    "EqualsHashCodeChecker testOK",
    """
      |package foobar
      |
      |class OK {
      |  def hashCode(): Int = 45
      |  def equals(o: java.lang.Object): Boolean = false
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class OK {
      |  def hashCode(): Int = 45
      |  def equals(o: java.lang.Object): Boolean = false
      |}
      |""".stripMargin
  )

  check(
    new EqualsHashCodeChecker,
    "EqualsHashCodeChecker testHashCodeOnlyKO",
    """
      |package foobar
      |
      |class HashCodeOnlyKO {
      |  def hashCode(): Int = 45
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class HashCodeOnlyKO/* scalafix:ok */ {
      |  def hashCode(): Int = 45
      |}
      |""".stripMargin
  )

  check(
    new EqualsHashCodeChecker,
    "EqualsHashCodeChecker testEqualsOnlyKO",
    """
      |package foobar
      |
      |class EqualsOnlyKO {
      |  def equals(o: java.lang.Object): Boolean = false
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class EqualsOnlyKO/* scalafix:ok */ {
      |  def equals(o: java.lang.Object): Boolean = false
      |}
      |""".stripMargin
  )

  check(
    new EqualsHashCodeChecker,
    "EqualsHashCodeChecker testEqualsOnlyAnyKO",
    """
      |package foobar
      |
      |class EqualsOnlyKO {
      |  def equals(o: Any): Boolean = false
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class EqualsOnlyKO/* scalafix:ok */ {
      |  def equals(o: Any): Boolean = false
      |}
      |""".stripMargin
  )

  check(
    new EqualsHashCodeChecker,
    "EqualsHashCodeChecker testEqualsWrongSignatureOK",
    """
      |package foobar
      |
      |class EqualsWrongSignatureOK {
      |  def equals(o: scala.Object): Boolean = false
      |  def equals(o: java.lang.Object)(o2: java.lang.Object): Boolean = false
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class EqualsWrongSignatureOK {
      |  def equals(o: scala.Object): Boolean = false
      |  def equals(o: java.lang.Object)(o2: java.lang.Object): Boolean = false
      |}
      |""".stripMargin
  )

  check(
    new EqualsHashCodeChecker,
    "EqualsHashCodeChecker testHashCodeWrongSignatureOK",
    """
      |package foobar
      |
      |class HashCodeWrongSignatureOK {
      |  def hashCode(o: scala.Object): Int = 45
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class HashCodeWrongSignatureOK {
      |  def hashCode(o: scala.Object): Int = 45
      |}
      |""".stripMargin
  )

  check(
    new EqualsHashCodeChecker,
    "EqualsHashCodeChecker testOuterKOInnerKO",
    """
      |package foobar
      |
      |class OuterKO {
      |  def hashCode(): Int = 45
      |  class InnerKO {
      |    def equals(o: java.lang.Object): Boolean = false
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class OuterKO/* scalafix:ok */ {
      |  def hashCode(): Int = 45
      |  class InnerKO/* scalafix:ok */ {
      |    def equals(o: java.lang.Object): Boolean = false
      |  }
      |}
      |""".stripMargin
  )

  check(
    new EqualsHashCodeChecker,
    "EqualsHashCodeChecker testOuterOKInnerKO",
    """
      |package foobar
      |
      |class OuterOK {
      |  def hashCode(): Int = 45
      |  class InnerKO {
      |    def equals(o: java.lang.Object): Boolean = false
      |  }
      |  def equals(o: java.lang.Object): Boolean = false
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class OuterOK {
      |  def hashCode(): Int = 45
      |  class InnerKO/* scalafix:ok */ {
      |    def equals(o: java.lang.Object): Boolean = false
      |  }
      |  def equals(o: java.lang.Object): Boolean = false
      |}
      |""".stripMargin
  )

  check(
    new EqualsHashCodeChecker,
    "EqualsHashCodeChecker testObjectInnerKO",
    """
      |package foobar
      |
      |object Object {
      |  class ObjectInnerKO {
      |    def equals(o: java.lang.Object): Boolean = false
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |object Object {
      |  class ObjectInnerKO/* scalafix:ok */ {
      |    def equals(o: java.lang.Object): Boolean = false
      |  }
      |}
      |""".stripMargin
  )

  check(
    new EqualsHashCodeChecker,
    "EqualsHashCodeChecker testMultipleClasses",
    """
      |package foobar
      |
      |class Class1 {
      |  def hashCode(): Int = 45
      |}
      |
      |class Class2 {
      |  def hashCode(): Int = 45
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class Class1/* scalafix:ok */ {
      |  def hashCode(): Int = 45
      |}
      |
      |class Class2/* scalafix:ok */ {
      |  def hashCode(): Int = 45
      |}
      |""".stripMargin
  )
}
