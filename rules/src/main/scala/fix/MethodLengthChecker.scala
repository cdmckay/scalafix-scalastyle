package fix

import metaconfig.ConfDecoder
import metaconfig.Configured
import scala.meta.Decl
import scala.meta.Defn
import scala.meta.Position
import scalafix.v1.Configuration
import scalafix.v1.Diagnostic
import scalafix.v1.Patch
import scalafix.v1.Rule
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

final case class MethodLengthCheckerConfig(
    maxLength: Int = 50,
    ignoreComments: Boolean = false,
    ignoreEmpty: Boolean = false
)

object MethodLengthCheckerConfig {
  val default: MethodLengthCheckerConfig = MethodLengthCheckerConfig()

  implicit val surface: metaconfig.generic.Surface[MethodLengthCheckerConfig] =
    metaconfig.generic.deriveSurface[MethodLengthCheckerConfig]

  implicit val decoder: ConfDecoder[MethodLengthCheckerConfig] =
    metaconfig.generic.deriveDecoder(default)
}

class MethodLengthChecker(config: MethodLengthCheckerConfig)
    extends SyntacticRule("MethodLengthChecker") {
  def this() = this(MethodLengthCheckerConfig.default)

  override def withConfiguration(conf: Configuration): Configured[Rule] =
    conf.conf
      .getOrElse("MethodLengthChecker")(config)
      .map(new MethodLengthChecker(_))

  override def fix(implicit doc: SyntacticDocument): Patch = {
    val lines = doc.input.text.linesIterator.toVector

    doc.tree.collect {
      case method: Defn.Def
          if MethodLengthChecker.countMethodLines(method.pos, lines, config) > config.maxLength =>
        Patch.lint(
          MethodLengthDiagnostic(method.name.pos, config.maxLength.toString)
        )
      case method: Decl.Def
          if MethodLengthChecker.countMethodLines(method.pos, lines, config) > config.maxLength =>
        Patch.lint(
          MethodLengthDiagnostic(method.name.pos, config.maxLength.toString)
        )
    }.asPatch
  }
}

object MethodLengthChecker {
  private val SingleLineComment = "//"
  private val MultiLineCommentOpener = "/*"
  private val MultiLineCommentCloser = "*/"

  private[fix] def countMethodLines(
      pos: Position,
      lines: Vector[String],
      config: MethodLengthCheckerConfig
  ): Int = {
    val startLine = pos.startLine + 1
    val endLine = pos.endLine + 1

    if (config.ignoreComments) {
      countIgnoringComments(lines, startLine, endLine, config.ignoreEmpty)
    } else {
      val emptyLines =
        if (config.ignoreEmpty) {
          lines
            .slice(startLine, endLine - 1)
            .count(_.isEmpty)
        } else {
          0
        }

      (endLine - startLine - 1) - emptyLines
    }
  }

  private def countIgnoringComments(
      lines: Vector[String],
      startLine: Int,
      endLine: Int,
      ignoreEmpty: Boolean
  ): Int = {
    var count = 0
    var multiLineComment = false

    for (lineNo <- (startLine + 1) until endLine) {
      val text = lines(lineNo - 1).trim

      if (ignoreEmpty && text.isEmpty) {
        ()
      } else if (text.startsWith(SingleLineComment)) {
        ()
      } else {
        if (text.contains(MultiLineCommentOpener)) {
          multiLineComment = true
        }
        if (!multiLineComment) {
          count += 1
        }
        if (text.contains(MultiLineCommentCloser)) {
          multiLineComment = false
        }
      }
    }

    count
  }
}

final case class MethodLengthDiagnostic(position: Position, maximum: String)
    extends Diagnostic {
  override def message: String =
    s"Method exceeds maximum length of $maximum lines."
}
