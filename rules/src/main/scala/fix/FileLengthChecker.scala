package fix

import metaconfig.ConfDecoder
import metaconfig.Configured
import scala.meta.Position
import scalafix.v1.Configuration
import scalafix.v1.Diagnostic
import scalafix.v1.Patch
import scalafix.v1.Rule
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

final case class FileLengthCheckerConfig(maxFileLength: Int = 1000)

object FileLengthCheckerConfig {
  val default: FileLengthCheckerConfig = FileLengthCheckerConfig()

  implicit val surface
      : metaconfig.generic.Surface[FileLengthCheckerConfig] =
    metaconfig.generic.deriveSurface[FileLengthCheckerConfig]

  implicit val decoder: ConfDecoder[FileLengthCheckerConfig] =
    metaconfig.generic.deriveDecoder(default)
}

class FileLengthChecker(config: FileLengthCheckerConfig)
    extends SyntacticRule("FileLengthChecker") {
  def this() = this(FileLengthCheckerConfig.default)

  override def withConfiguration(conf: Configuration): Configured[Rule] =
    conf.conf
      .getOrElse("FileLengthChecker")(config)
      .map(new FileLengthChecker(_))

  override def fix(implicit doc: SyntacticDocument): Patch = {
    val lines = doc.input.text.linesIterator.length

    if (lines > config.maxFileLength) {
      val position = doc.tokens.headOption.map(_.pos).getOrElse(doc.tree.pos)
      Patch.lint(FileLengthDiagnostic(position, config.maxFileLength.toString))
    } else {
      Patch.empty
    }
  }
}

final case class FileLengthDiagnostic(position: Position, maximum: String)
    extends Diagnostic {
  override def message: String =
    s"File exceeds maximum length of $maximum lines."
}
