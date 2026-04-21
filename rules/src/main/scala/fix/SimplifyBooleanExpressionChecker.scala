package fix

import scala.meta.Lit
import scala.meta.Position
import scala.meta.Term
import scala.meta.Tree
import scalafix.v1.Diagnostic
import scalafix.v1.Patch
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

class SimplifyBooleanExpressionChecker
    extends SyntacticRule("SimplifyBooleanExpressionChecker") {
  override def fix(implicit doc: SyntacticDocument): Patch =
    doc.tree.collect {
      case expr: Term.ApplyInfix
          if SimplifyBooleanExpressionChecker.hasRedundantBooleanLiteral(expr) =>
        Patch.lint(SimplifyBooleanExpressionDiagnostic(expr.lhs.pos))
    }.asPatch + SimplifyBooleanExpressionChecker.negatedBooleanPatches(doc)
}

object SimplifyBooleanExpressionChecker {
  private val BooleanOperators = Set("==", "!=", "&&", "||")

  private[fix] def hasRedundantBooleanLiteral(expr: Term.ApplyInfix): Boolean =
    BooleanOperators.contains(expr.op.value) &&
      (isBooleanLiteral(expr.lhs) || expr.args.exists(isBooleanLiteral))

  private[fix] def negatedBooleanPatches(doc: SyntacticDocument): Patch =
    doc.tokens
      .sliding(2)
      .collect {
        case Seq(bang, bool)
            if bang.text == "!" && Set("true", "false").contains(bool.text) =>
          Patch.lint(SimplifyBooleanExpressionDiagnostic(bang.pos))
      }
      .foldLeft(Patch.empty)(_ + _)

  private def isBooleanLiteral(tree: Tree): Boolean = tree match {
    case Lit.Boolean(_) =>
      true
    case Term.Tuple(List(inner)) =>
      isBooleanLiteral(inner)
    case _ =>
      false
  }
}

final case class SimplifyBooleanExpressionDiagnostic(position: Position)
    extends Diagnostic {
  override def message: String = "Boolean expression can be simplified."
}
