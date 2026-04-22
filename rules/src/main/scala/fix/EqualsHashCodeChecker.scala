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

class EqualsHashCodeChecker extends SyntacticRule("EqualsHashCodeChecker") {
  override def fix(implicit doc: SyntacticDocument): Patch =
    EqualsHashCodeChecker.types(doc.tree).collect {
      case tpe if EqualsHashCodeChecker.hasMismatch(tpe) =>
        Patch.lint(EqualsHashCodeDiagnostic(EqualsHashCodeChecker.namePos(tpe)))
    }.asPatch
}

object EqualsHashCodeChecker {
  private[fix] def types(tree: Tree): List[Tree] =
    tree.collect {
      case d: Defn.Class => d: Tree
      case d: Defn.Object => d: Tree
      case d: Pkg.Object => d: Tree
    }

  private[fix] def hasMismatch(tree: Tree): Boolean = {
    val methods = directMethods(tree)
    val hasHashCode = methods.exists(isHashCode)
    val hasEqualsObject = methods.exists(isEqualsObject)
    (hasHashCode && !hasEqualsObject) || (!hasHashCode && hasEqualsObject)
  }

  private def directMethods(tree: Tree): List[Defn.Def] =
    templateStats(tree).collect { case d: Defn.Def => d }

  private def templateStats(tree: Tree): List[scala.meta.Stat] = tree match {
    case d: Defn.Class => d.templ.stats
    case d: Defn.Object => d.templ.stats
    case d: Pkg.Object => d.templ.stats
    case _ => Nil
  }

  private[fix] def isHashCode(method: Defn.Def): Boolean =
    method.name.value == "hashCode" && methodParamCount(method) == 0

  private[fix] def isEqualsObject(method: Defn.Def): Boolean =
    method.name.value == "equals" &&
      methodParamCount(method) == 1 &&
      firstParameter(method).exists(isObjectParam)

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

final case class EqualsHashCodeDiagnostic(position: Position) extends Diagnostic {
  override def message: String =
    "Types that define equals should also define hashCode, and vice versa."
}
