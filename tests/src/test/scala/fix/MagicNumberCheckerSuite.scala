package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class MagicNumberCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  check(
    new MagicNumberChecker,
    "MagicNumberChecker testVal",
    """
      |package foobar
      |
      |class Foobar {
      |
      |  val foo0 = -2
      |  val foo1 = -1
      |  val foo2 = 0
      |  val foo3 = 1
      |  val foo4 = 2
      |  val foo5 = 3
      |  val foo6 = 4
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class Foobar {
      |
      |  val foo0 = -2
      |  val foo1 = -1
      |  val foo2 = 0
      |  val foo3 = 1
      |  val foo4 = 2
      |  val foo5 = 3
      |  val foo6 = 4
      |}
      |""".stripMargin
  )

  check(
    new MagicNumberChecker,
    "MagicNumberChecker testVar",
    """
      |package foobar
      |
      |class Foobar {
      |  var foo0 = -2
      |  var foo1 = -1
      |  var foo2 = 0
      |  var foo3 = 1
      |  var foo4 = 2
      |  var foo5 = 3
      |  var foo6 = 4
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class Foobar {
      |  var foo0 = -/* scalafix:ok */2
      |  var foo1 = -1
      |  var foo2 = 0
      |  var foo3 = 1
      |  var foo4 = 2
      |  var foo5 = 3/* scalafix:ok */
      |  var foo6 = 4/* scalafix:ok */
      |}
      |""".stripMargin
  )

  check(
    new MagicNumberChecker,
    "MagicNumberChecker testVar2",
    """
      |package foobar
      |
      |class Foobar {
      |  var foo6 = 4
      |  var foo7 = +4
      |  var foo8 = -4
      |  var bar1 = fn(7, -5)
      |  var bar2 = fn(1, -5)
      |
      |  def fn(i: Int, j: Int) = i + j
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class Foobar {
      |  var foo6 = 4/* scalafix:ok */
      |  var foo7 = +/* scalafix:ok */4
      |  var foo8 = -/* scalafix:ok */4
      |  var bar1 = fn(7/* scalafix:ok */, -/* scalafix:ok */5)
      |  var bar2 = fn(1, -/* scalafix:ok */5)
      |
      |  def fn(i: Int, j: Int) = i + j
      |}
      |""".stripMargin
  )

  check(
    new MagicNumberChecker(MagicNumberCheckerConfig(ignore = "-1,0,1,2,100 ")),
    "MagicNumberChecker testIgnoreParamShouldTolerateSpaces",
    """
      |package foobar
      |
      |class Foobar {
      |
      |  var fooOk: Long = 1
      |  var fooFail: Long = 100L
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class Foobar {
      |
      |  var fooOk: Long = 1
      |  var fooFail: Long = 100L
      |}
      |""".stripMargin
  )
}
