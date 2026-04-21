package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class MethodLengthCheckerSuite extends AbstractSyntacticRuleSuite with AnyFunSuiteLike {
  private val max7 = new MethodLengthChecker(MethodLengthCheckerConfig(maxLength = 7))
  private val max5IgnoreComments =
    new MethodLengthChecker(
      MethodLengthCheckerConfig(maxLength = 5, ignoreComments = true)
    )
  private val max5IgnoreEmpty =
    new MethodLengthChecker(
      MethodLengthCheckerConfig(maxLength = 5, ignoreEmpty = true)
    )
  private val max4IgnoreEmpty =
    new MethodLengthChecker(
      MethodLengthCheckerConfig(maxLength = 4, ignoreEmpty = true)
    )
  private val max5IgnoreCommentsAndEmpty =
    new MethodLengthChecker(
      MethodLengthCheckerConfig(
        maxLength = 5,
        ignoreComments = true,
        ignoreEmpty = true
      )
    )
  private val max5DontIgnoreComments =
    new MethodLengthChecker(
      MethodLengthCheckerConfig(maxLength = 5, ignoreComments = false)
    )

  check(
    max7,
    "MethodLengthChecker testOK",
    """
      |package foobar
      |
      |class F1() {
      |  def method1() = {
      |    def foobar() = { 5 }
      |    2
      |    3
      |    4
      |    5
      |    6
      |    7
      |  }
      |  def method2() = {
      |    1
      |    2
      |    3
      |    4
      |    5
      |    6
      |  }
      |  def method3() = {
      |    def foobar() = { 5 }
      |    2
      |    3
      |    4
      |    5
      |    6
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class F1() {
      |  def method1() = {
      |    def foobar() = { 5 }
      |    2
      |    3
      |    4
      |    5
      |    6
      |    7
      |  }
      |  def method2() = {
      |    1
      |    2
      |    3
      |    4
      |    5
      |    6
      |  }
      |  def method3() = {
      |    def foobar() = { 5 }
      |    2
      |    3
      |    4
      |    5
      |    6
      |  }
      |}
      |""".stripMargin
  )

  check(
    max5IgnoreComments,
    "MethodLengthChecker testIgnoreComments",
    """
      |package foobar
      |
      |class F2() {
      |  def method1() = {
      |    1
      |    2
      |    /** (3)
      |     *  (4)
      |     */ (5)
      |    3   (6)
      |    4   (7)
      |    5   (8)
      |  }
      |  def method2() = {
      |    // (1)
      |    1  (2)
      |    2  (3)
      |    // (4)
      |    // (5)
      |    3  (6)
      |    4  (7)
      |    5  (8)
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class F2() {
      |  def method1() = {
      |    1
      |    2
      |    /** (3)
      |     *  (4)
      |     */ (5)
      |    3   (6)
      |    4   (7)
      |    5   (8)
      |  }
      |  def method2() = {
      |    // (1)
      |    1  (2)
      |    2  (3)
      |    // (4)
      |    // (5)
      |    3  (6)
      |    4  (7)
      |    5  (8)
      |  }
      |}
      |""".stripMargin
  )

  check(
    max5IgnoreEmpty,
    "MethodLengthChecker testIgnoreEmpty",
    """
      |package foobar
      |
      |class F2() {
      |  def method1() = {
      |    1  (1)
      |    2  (2)
      |
      |    3  (4)
      |    4  (5)
      |    5  (6)
      |  }
      |  def method2() = {
      |
      |    1 (2)
      |    2 (3)
      |    3 (4)
      |    4 (5)
      |    5 (6)
      |
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class F2() {
      |  def method1() = {
      |    1  (1)
      |    2  (2)
      |
      |    3  (4)
      |    4  (5)
      |    5  (6)
      |  }
      |  def method2() = {
      |
      |    1 (2)
      |    2 (3)
      |    3 (4)
      |    4 (5)
      |    5 (6)
      |
      |  }
      |}
      |""".stripMargin
  )

  check(
    max4IgnoreEmpty,
    "MethodLengthChecker testIgnoreEmptyWithErrors",
    """
      |package foobar
      |
      |class F2() {
      |  def method1() = {
      |    1  (1)
      |    2  (2)
      |
      |    3  (4)
      |    4  (5)
      |    5  (6)
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class F2() {
      |  def method1/* scalafix:ok */() = {
      |    1  (1)
      |    2  (2)
      |
      |    3  (4)
      |    4  (5)
      |    5  (6)
      |  }
      |}
      |""".stripMargin
  )

  check(
    max5IgnoreCommentsAndEmpty,
    "MethodLengthChecker testIgnoreEmptyAndComments",
    """
      |package foobar
      |
      |class F2() {
      |  def method1() = {
      |    1  (1)
      |    2  (2)
      |
      |    3  (4)
      |    4  (5)
      |    5  (6)
      |    // (7)
      |  }
      |  def method2() = {
      |
      |    1   (2)
      |    2   (3)
      |    //  (4)
      |
      |    3   (6)
      |    4   (7)
      |    /** (8)
      |     *  (9)
      |     */ (10)
      |
      |    5   (12)
      |
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class F2() {
      |  def method1() = {
      |    1  (1)
      |    2  (2)
      |
      |    3  (4)
      |    4  (5)
      |    5  (6)
      |    // (7)
      |  }
      |  def method2() = {
      |
      |    1   (2)
      |    2   (3)
      |    //  (4)
      |
      |    3   (6)
      |    4   (7)
      |    /** (8)
      |     *  (9)
      |     */ (10)
      |
      |    5   (12)
      |
      |  }
      |}
      |""".stripMargin
  )

  check(
    max5DontIgnoreComments,
    "MethodLengthChecker testNotIgnoreComments",
    """
      |package foobar
      |
      |class F2() {
      |  def method1() = {
      |    1
      |    2
      |    /** (3)
      |     *  (4)
      |     */ (5)
      |    3   (6)
      |  }
      |  def method2() = {
      |    // (1)
      |    1  (2)
      |    2  (3)
      |    // (4)
      |    // (5)
      |    3  (6)
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class F2() {
      |  def method1/* scalafix:ok */() = {
      |    1
      |    2
      |    /** (3)
      |     *  (4)
      |     */ (5)
      |    3   (6)
      |  }
      |  def method2/* scalafix:ok */() = {
      |    // (1)
      |    1  (2)
      |    2  (3)
      |    // (4)
      |    // (5)
      |    3  (6)
      |  }
      |}
      |""".stripMargin
  )

  check(
    max5IgnoreComments,
    "MethodLengthChecker testIgnoreCommentsComplicated",
    """
      |class F3() {
      |  def method1() = {
      |    1 //
      |    (2) /*  */
      |    2
      |    3
      |    (4) /*
      |         *  (5)
      |         */ (6)
      |    4 (7)
      |    5 (8)
      |  }
      |  def method2() = {
      |    // /* (1)
      |    1     (2)
      |    2     (3)
      |    3     (5)
      |    4     (6)
      |    5     (7)
      |    6     (8)
      |  }
      |}
      |""".stripMargin,
    """
      |class F3() {
      |  def method1() = {
      |    1 //
      |    (2) /*  */
      |    2
      |    3
      |    (4) /*
      |         *  (5)
      |         */ (6)
      |    4 (7)
      |    5 (8)
      |  }
      |  def method2/* scalafix:ok */() = {
      |    // /* (1)
      |    1     (2)
      |    2     (3)
      |    3     (5)
      |    4     (6)
      |    5     (7)
      |    6     (8)
      |  }
      |}
      |""".stripMargin
  )
}
