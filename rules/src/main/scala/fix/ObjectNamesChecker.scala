package fix

import metaconfig.ConfDecoder
import metaconfig.Configured
import scala.meta.Defn
import scala.meta.Pkg
import scala.meta.Position
import scalafix.v1.Configuration
import scalafix.v1.Diagnostic
import scalafix.v1.Patch
import scalafix.v1.Rule
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

final case class ObjectNamesCheckerConfig(regex: String = "^[A-Z][A-Za-z]*$")

object ObjectNamesCheckerConfig {
  val default: ObjectNamesCheckerConfig = ObjectNamesCheckerConfig()

  implicit val surface
      : metaconfig.generic.Surface[ObjectNamesCheckerConfig] =
    metaconfig.generic.deriveSurface[ObjectNamesCheckerConfig]

  implicit val decoder: ConfDecoder[ObjectNamesCheckerConfig] =
    metaconfig.generic.deriveDecoder(default)
}

class ObjectNamesChecker(config: ObjectNamesCheckerConfig)
    extends SyntacticRule("ObjectNamesChecker") {
  def this() = this(ObjectNamesCheckerConfig.default)

  override def withConfiguration(conf: Configuration): Configured[Rule] =
    conf.conf
      .getOrElse("ObjectNamesChecker")(config)
      .map(new ObjectNamesChecker(_))

  override def fix(implicit doc: SyntacticDocument): Patch = {
    val regex = config.regex.r

    doc.tree.collect {
      case obj: Defn.Object if regex.findFirstIn(obj.name.value).isEmpty =>
        Patch.lint(ObjectNamesDiagnostic(obj.name.pos, config.regex))
      case _: Pkg.Object =>
        Patch.empty
    }.asPatch
  }
}

final case class ObjectNamesDiagnostic(position: Position, regex: String)
    extends Diagnostic {
  override def message: String =
    s"Object name does not match regex: $regex"
}
