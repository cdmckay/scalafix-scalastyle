package fix

import scala.meta.Pat
import scala.meta.Position
import scalafix.v1._

class LowercasePatternMatchChecker
    extends SyntacticRule("LowercasePatternMatchChecker") {
  override def fix(implicit doc: SyntacticDocument): Patch =
    doc.tree.collect {
      case c @ scala.meta.Case(pattern, _, _)
          if LowercasePatternMatchChecker.isLowercaseSimplePattern(pattern) =>
        Patch.lint(LowercasePatternMatchDiagnostic(c.pat.pos))
    }.asPatch
}

object LowercasePatternMatchChecker {
  private[fix] def isLowercaseSimplePattern(pattern: Pat): Boolean =
    pattern match {
      case Pat.Var(name) =>
        name.value.nonEmpty && name.value.charAt(0).isLower
      case _ =>
        false
    }
}

final case class LowercasePatternMatchDiagnostic(position: Position)
    extends Diagnostic {
  override def message: String =
    "Avoid lowercase simple pattern matches; use backticks for stable identifiers."
}
