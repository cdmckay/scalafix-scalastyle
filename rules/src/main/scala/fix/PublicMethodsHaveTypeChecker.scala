package fix

import metaconfig.ConfDecoder
import metaconfig.Configured
import scala.meta.Defn
import scala.meta.Mod
import scala.meta.Pkg
import scala.meta.Position
import scala.meta.Stat
import scala.meta.Tree
import scalafix.v1.Configuration
import scalafix.v1.Diagnostic
import scalafix.v1.Patch
import scalafix.v1.Rule
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

final case class PublicMethodsHaveTypeCheckerConfig(
    ignoreOverride: Boolean = false
)

object PublicMethodsHaveTypeCheckerConfig {
  val default: PublicMethodsHaveTypeCheckerConfig =
    PublicMethodsHaveTypeCheckerConfig()

  implicit val surface
      : metaconfig.generic.Surface[PublicMethodsHaveTypeCheckerConfig] =
    metaconfig.generic.deriveSurface[PublicMethodsHaveTypeCheckerConfig]

  implicit val decoder: ConfDecoder[PublicMethodsHaveTypeCheckerConfig] =
    metaconfig.generic.deriveDecoder(default)
}

class PublicMethodsHaveTypeChecker(config: PublicMethodsHaveTypeCheckerConfig)
    extends SyntacticRule("PublicMethodsHaveTypeChecker") {
  def this() = this(PublicMethodsHaveTypeCheckerConfig.default)

  override def withConfiguration(conf: Configuration): Configured[Rule] =
    conf.conf
      .getOrElse("PublicMethodsHaveTypeChecker")(config)
      .map(new PublicMethodsHaveTypeChecker(_))

  override def fix(implicit doc: SyntacticDocument): Patch =
    PublicMethodsHaveTypeChecker
      .types(doc.tree)
      .flatMap(PublicMethodsHaveTypeChecker.directMethods)
      .collect {
        case method
            if PublicMethodsHaveTypeChecker.shouldLint(
              method,
              config.ignoreOverride
            ) =>
          Patch.lint(PublicMethodsHaveTypeDiagnostic(method.name.pos))
      }
      .asPatch
}

object PublicMethodsHaveTypeChecker {
  private[fix] def types(tree: Tree): List[Tree] =
    tree.collect {
      case d: Defn.Class => d: Tree
      case d: Defn.Trait => d: Tree
      case d: Defn.Object => d: Tree
      case d: Pkg.Object => d: Tree
    }

  private[fix] def directMethods(tree: Tree): List[Defn.Def] =
    templateStats(tree).collect { case d: Defn.Def => d }

  private def templateStats(tree: Tree): List[Stat] = tree match {
    case d: Defn.Class => d.templ.stats
    case d: Defn.Trait => d.templ.stats
    case d: Defn.Object => d.templ.stats
    case d: Pkg.Object => d.templ.stats
    case _ => Nil
  }

  private[fix] def shouldLint(
      method: Defn.Def,
      ignoreOverride: Boolean
  ): Boolean =
    method.decltpe.isEmpty &&
      !hasRestrictedVisibility(method) &&
      !(ignoreOverride && hasOverride(method)) &&
      !isProcedureSyntax(method)

  private def hasRestrictedVisibility(method: Defn.Def): Boolean =
    method.mods.exists {
      case _: Mod.Private => true
      case _: Mod.Protected => true
      case _ => false
    }

  private def hasOverride(method: Defn.Def): Boolean =
    method.mods.exists {
      case _: Mod.Override => true
      case _ => false
    }

  private def isProcedureSyntax(method: Defn.Def): Boolean =
    !method.tokens.exists(_.text == "=")
}

final case class PublicMethodsHaveTypeDiagnostic(position: Position)
    extends Diagnostic {
  override def message: String =
    "Public methods should have an explicit return type."
}
