package fix

import scala.meta.Defn
import scala.meta.Pkg
import scala.meta.Position
import scala.meta.Term
import scala.meta.Tree
import scalafix.v1.Diagnostic
import scalafix.v1.Patch
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

class CovariantEqualsChecker extends SyntacticRule("CovariantEqualsChecker") {
  override def fix(implicit doc: SyntacticDocument): Patch =
    CovariantEqualsChecker.types(doc.tree).collect {
      case tpe if CovariantEqualsChecker.hasCovariantEqualsOnly(tpe) =>
        Patch.lint(CovariantEqualsDiagnostic(CovariantEqualsChecker.namePos(tpe)))
    }.asPatch
}

object CovariantEqualsChecker {
  private[fix] def types(tree: Tree): List[Tree] =
    tree.collect {
      case d: Defn.Class => d: Tree
      case d: Defn.Object => d: Tree
      case d: Pkg.Object => d: Tree
    }

  private[fix] def hasCovariantEqualsOnly(tree: Tree): Boolean = {
    val methods = directMethods(tree)
    val equalsObject = methods.exists(EqualsHashCodeChecker.isEqualsObject)
    val equalsOther = methods.exists(isEqualsOther)
    !equalsObject && equalsOther
  }

  private def directMethods(tree: Tree): List[Defn.Def] =
    tree match {
      case d: Defn.Class => d.templ.stats.collect { case m: Defn.Def => m }
      case d: Defn.Object => d.templ.stats.collect { case m: Defn.Def => m }
      case d: Pkg.Object => d.templ.stats.collect { case m: Defn.Def => m }
      case _ => Nil
    }

  private def isEqualsOther(method: Defn.Def): Boolean =
    method.name.value == "equals" &&
      methodParamCount(method) == 1 &&
      firstParameter(method).exists(param => !isObjectParam(param))

  private def methodParamCount(method: Defn.Def): Int =
    method.collect {
      case param: Term.Param if param.pos.end <= method.body.pos.start => param
    }.size

  private def firstParameter(method: Defn.Def): Option[Term.Param] =
    method.collect {
      case param: Term.Param if param.pos.end <= method.body.pos.start => param
    }.headOption

  private def isObjectParam(param: Term.Param): Boolean =
    param.decltpe.exists { tpe =>
      val syntax = tpe.syntax
      syntax == "java.lang.Object" || syntax == "Any"
    }

  private[fix] def namePos(tree: Tree): Position = tree match {
    case d: Defn.Class => d.name.pos
    case d: Defn.Object => d.name.pos
    case d: Pkg.Object => d.name.pos
  }
}

final case class CovariantEqualsDiagnostic(position: Position) extends Diagnostic {
  override def message: String =
    "Covariant equals should be paired with equals(Object)."
}
