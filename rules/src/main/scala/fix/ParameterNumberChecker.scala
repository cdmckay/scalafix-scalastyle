package fix

import metaconfig.ConfDecoder
import metaconfig.Configured
import scala.meta.Decl
import scala.meta.Defn
import scala.meta.Position
import scala.meta.Term
import scala.meta.Tree
import scalafix.v1.Configuration
import scalafix.v1.Diagnostic
import scalafix.v1.Patch
import scalafix.v1.Rule
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

final case class ParameterNumberCheckerConfig(maxParameters: Int = 8)

object ParameterNumberCheckerConfig {
  val default: ParameterNumberCheckerConfig = ParameterNumberCheckerConfig()

  implicit val surface
      : metaconfig.generic.Surface[ParameterNumberCheckerConfig] =
    metaconfig.generic.deriveSurface[ParameterNumberCheckerConfig]

  implicit val decoder: ConfDecoder[ParameterNumberCheckerConfig] =
    metaconfig.generic.deriveDecoder(default)
}

class ParameterNumberChecker(config: ParameterNumberCheckerConfig)
    extends SyntacticRule("ParameterNumberChecker") {
  def this() = this(ParameterNumberCheckerConfig.default)

  override def withConfiguration(conf: Configuration): Configured[Rule] =
    conf.conf
      .getOrElse("ParameterNumberChecker")(config)
      .map(new ParameterNumberChecker(_))

  override def fix(implicit doc: SyntacticDocument): Patch =
    doc.tree.collect {
      case method: Defn.Def
          if ParameterNumberChecker.countParameters(method) > config.maxParameters =>
        Patch.lint(
          ParameterNumberDiagnostic(method.name.pos, config.maxParameters.toString)
        )
      case method: Decl.Def
          if ParameterNumberChecker.countParameters(method) > config.maxParameters =>
        Patch.lint(
          ParameterNumberDiagnostic(method.name.pos, config.maxParameters.toString)
        )
    }.asPatch
}

object ParameterNumberChecker {
  private[fix] def countParameters(method: Tree): Int = method match {
    case method: Defn.Def =>
      method.collect {
        case param: Term.Param if param.pos.end <= method.body.pos.start => param
      }.size
    case method: Decl.Def =>
      method.collect {
        case param: Term.Param => param
      }.size
    case _ =>
      0
  }
}

final case class ParameterNumberDiagnostic(position: Position, maximum: String)
    extends Diagnostic {
  override def message: String =
    s"Method has more than $maximum parameters."
}
