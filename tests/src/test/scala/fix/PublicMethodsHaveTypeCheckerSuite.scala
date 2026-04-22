package fix

import org.scalatest.funsuite.AnyFunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite

class PublicMethodsHaveTypeCheckerSuite
    extends AbstractSyntacticRuleSuite
    with AnyFunSuiteLike {
  check(
    new PublicMethodsHaveTypeChecker,
    "PublicMethodsHaveTypeChecker testClassOK",
    """
      |package foobar
      |
      |class OK {
      |  def c1() = 5
      |  def c2(): Int = 5
      |  def c3 = 5
      |  protected def c4() = 5
      |  private def c5() = 5
      |  private[this] def c6() = 5
      |  private val foo1 = 1
      |  val foo2 = 2
      |  def unit = {}
      |  def unit2 {}
      |  val foo = new scala.collection.mutable.HashMap {def foobar1() = {}}
      |  def bar() = { new scala.collection.mutable.HashMap {def foobar2() = {}} }
      |  def bar2() = new scala.collection.mutable.HashMap {def foobar3() = {}}
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |class OK {
      |  def c1/* scalafix:ok */() = 5
      |  def c2(): Int = 5
      |  def c3/* scalafix:ok */ = 5
      |  protected def c4() = 5
      |  private def c5() = 5
      |  private[this] def c6() = 5
      |  private val foo1 = 1
      |  val foo2 = 2
      |  def unit/* scalafix:ok */ = {}
      |  def unit2 {}
      |  val foo = new scala.collection.mutable.HashMap {def foobar1() = {}}
      |  def bar/* scalafix:ok */() = { new scala.collection.mutable.HashMap {def foobar2() = {}} }
      |  def bar2/* scalafix:ok */() = new scala.collection.mutable.HashMap {def foobar3() = {}}
      |}
      |""".stripMargin
  )

  check(
    new PublicMethodsHaveTypeChecker,
    "PublicMethodsHaveTypeChecker testProc",
    """
      |class classOK {
      |  def proc1 {}
      |  def proc2(): Unit = {}
      |}
      |
      |abstract class abstractOK {
      |  def proc1 {}
      |  def proc2(): Unit = {}
      |  def proc3()
      |}
      |
      |trait traitOK {
      |  def proc1 {}
      |  def proc2(): Unit = {}
      |  def proc3()
      |}
      |""".stripMargin,
    """
      |class classOK {
      |  def proc1 {}
      |  def proc2(): Unit = {}
      |}
      |
      |abstract class abstractOK {
      |  def proc1 {}
      |  def proc2(): Unit = {}
      |  def proc3()
      |}
      |
      |trait traitOK {
      |  def proc1 {}
      |  def proc2(): Unit = {}
      |  def proc3()
      |}
      |""".stripMargin
  )

  check(
    new PublicMethodsHaveTypeChecker(
      PublicMethodsHaveTypeCheckerConfig(ignoreOverride = true)
    ),
    "PublicMethodsHaveTypeChecker testClassOverrideIgnore",
    """
      |package foobar
      |
      |trait Foobar {
      |  def foobar: Int
      |}
      |
      |class Sub extends Foobar {
      |  override def foobar() = 5
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |trait Foobar {
      |  def foobar: Int
      |}
      |
      |class Sub extends Foobar {
      |  override def foobar() = 5
      |}
      |""".stripMargin
  )

  check(
    new PublicMethodsHaveTypeChecker(
      PublicMethodsHaveTypeCheckerConfig(ignoreOverride = false)
    ),
    "PublicMethodsHaveTypeChecker testClassOverrideNoIgnore",
    """
      |package foobar
      |
      |trait Foobar {
      |  def foobar: Int
      |}
      |
      |class Sub extends Foobar {
      |  override def foobar() = 5
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |trait Foobar {
      |  def foobar: Int
      |}
      |
      |class Sub extends Foobar {
      |  override def foobar/* scalafix:ok */() = 5
      |}
      |""".stripMargin
  )

  check(
    new PublicMethodsHaveTypeChecker,
    "PublicMethodsHaveTypeChecker testNestedDefInDef",
    """
      |package foobar
      |
      |trait Foobar {
      |  def foobar() = {
      |    def nested1(): Int = 5
      |    def nested2() = 5
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |trait Foobar {
      |  def foobar/* scalafix:ok */() = {
      |    def nested1(): Int = 5
      |    def nested2() = 5
      |  }
      |}
      |""".stripMargin
  )

  check(
    new PublicMethodsHaveTypeChecker,
    "PublicMethodsHaveTypeChecker testNestedDefInVal",
    """
      |package foobar
      |
      |trait Foobar {
      |  val foobar = {
      |    def nested1(): Int = 5
      |    def nested2() = 5
      |
      |    nested2()
      |  }
      |}
      |""".stripMargin,
    """
      |package foobar
      |
      |trait Foobar {
      |  val foobar = {
      |    def nested1(): Int = 5
      |    def nested2() = 5
      |
      |    nested2()
      |  }
      |}
      |""".stripMargin
  )
}
