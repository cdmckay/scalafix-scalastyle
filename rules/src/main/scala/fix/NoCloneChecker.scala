package fix

import scala.meta.Defn
import scala.meta.Position
import scala.meta.Term
import scalafix.v1.Diagnostic
import scalafix.v1.Patch
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

class NoCloneChecker extends SyntacticRule("NoCloneChecker") {
  override def fix(implicit doc: SyntacticDocument): Patch =
    doc.tree.collect {
      case method: Defn.Def
          if method.name.value == "clone" && NoCloneChecker.hasNoParameters(method) =>
        Patch.lint(NoCloneDiagnostic(method.name.pos))
    }.asPatch
}

object NoCloneChecker {
  private[fix] def hasNoParameters(method: Defn.Def): Boolean =
    method.collect {
      case param: Term.Param if param.pos.end <= method.body.pos.start => param
    }.isEmpty
}

final case class NoCloneDiagnostic(position: Position) extends Diagnostic {
  override def message: String = "Override clone() is forbidden."
}
