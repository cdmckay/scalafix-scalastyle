package fix

import scala.meta.Position
import scala.meta.Tree
import scala.meta.Type
import scalafix.v1.Diagnostic
import scalafix.v1.Patch
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

class StructuralTypeChecker extends SyntacticRule("StructuralTypeChecker") {
  override def fix(implicit doc: SyntacticDocument): Patch =
    doc.tree.collect {
      case refinement: Type.Refine =>
        Patch.lint(StructuralTypeDiagnostic(refinement.pos))
    }.asPatch
}

final case class StructuralTypeDiagnostic(position: Position) extends Diagnostic {
  override def message: String = "Structural types are forbidden."
}
