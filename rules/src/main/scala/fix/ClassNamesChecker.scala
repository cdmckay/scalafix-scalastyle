package fix

import metaconfig.ConfDecoder
import metaconfig.Configured
import scala.meta.Defn
import scala.meta.Position
import scalafix.v1.Configuration
import scalafix.v1.Diagnostic
import scalafix.v1.Patch
import scalafix.v1.Rule
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

final case class ClassNamesCheckerConfig(regex: String = "^[A-Z][A-Za-z]*$")

object ClassNamesCheckerConfig {
  val default: ClassNamesCheckerConfig = ClassNamesCheckerConfig()

  implicit val surface
      : metaconfig.generic.Surface[ClassNamesCheckerConfig] =
    metaconfig.generic.deriveSurface[ClassNamesCheckerConfig]

  implicit val decoder: ConfDecoder[ClassNamesCheckerConfig] =
    metaconfig.generic.deriveDecoder(default)
}

class ClassNamesChecker(config: ClassNamesCheckerConfig)
    extends SyntacticRule("ClassNamesChecker") {
  def this() = this(ClassNamesCheckerConfig.default)

  override def withConfiguration(conf: Configuration): Configured[Rule] =
    conf.conf
      .getOrElse("ClassNamesChecker")(config)
      .map(new ClassNamesChecker(_))

  override def fix(implicit doc: SyntacticDocument): Patch = {
    val regex = config.regex.r

    doc.tree.collect {
      case cls: Defn.Class if regex.findFirstIn(cls.name.value).isEmpty =>
        Patch.lint(ClassNamesDiagnostic(cls.name.pos, config.regex))
    }.asPatch
  }
}

final case class ClassNamesDiagnostic(position: Position, regex: String)
    extends Diagnostic {
  override def message: String =
    s"Class name does not match regex: $regex"
}
