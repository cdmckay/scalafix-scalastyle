package fix

import metaconfig.ConfDecoder
import metaconfig.Configured
import scala.meta.Defn
import scala.meta.Pkg
import scala.meta.Position
import scala.meta.Tree
import scalafix.v1.Configuration
import scalafix.v1.Diagnostic
import scalafix.v1.Patch
import scalafix.v1.Rule
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

final case class NumberOfTypesCheckerConfig(maxTypes: Int = 30)

object NumberOfTypesCheckerConfig {
  val default: NumberOfTypesCheckerConfig = NumberOfTypesCheckerConfig()

  implicit val surface
      : metaconfig.generic.Surface[NumberOfTypesCheckerConfig] =
    metaconfig.generic.deriveSurface[NumberOfTypesCheckerConfig]

  implicit val decoder: ConfDecoder[NumberOfTypesCheckerConfig] =
    metaconfig.generic.deriveDecoder(default)
}

class NumberOfTypesChecker(config: NumberOfTypesCheckerConfig)
    extends SyntacticRule("NumberOfTypesChecker") {
  def this() = this(NumberOfTypesCheckerConfig.default)

  override def withConfiguration(conf: Configuration): Configured[Rule] =
    conf.conf
      .getOrElse("NumberOfTypesChecker")(config)
      .map(new NumberOfTypesChecker(_))

  override def fix(implicit doc: SyntacticDocument): Patch = {
    val typeCount = NumberOfTypesChecker.countTypes(doc.tree)

    if (typeCount > config.maxTypes) {
      val position = doc.tokens.headOption.map(_.pos).getOrElse(doc.tree.pos)
      Patch.lint(NumberOfTypesDiagnostic(position, config.maxTypes.toString))
    } else {
      Patch.empty
    }
  }
}

object NumberOfTypesChecker {
  private[fix] def countTypes(tree: Tree): Int =
    tree.collect {
      case _: Defn.Class => ()
      case _: Defn.Trait => ()
      case _: Defn.Object => ()
      case _: Pkg.Object => ()
    }.size
}

final case class NumberOfTypesDiagnostic(position: Position, maximum: String)
    extends Diagnostic {
  override def message: String =
    s"File contains more than $maximum types."
}
