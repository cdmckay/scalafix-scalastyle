package fix

import metaconfig.ConfDecoder
import metaconfig.Configured
import scala.meta.Defn
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

final case class NumberOfMethodsInTypeCheckerConfig(maxMethods: Int = 30)

object NumberOfMethodsInTypeCheckerConfig {
  val default: NumberOfMethodsInTypeCheckerConfig =
    NumberOfMethodsInTypeCheckerConfig()

  implicit val surface
      : metaconfig.generic.Surface[NumberOfMethodsInTypeCheckerConfig] =
    metaconfig.generic.deriveSurface[NumberOfMethodsInTypeCheckerConfig]

  implicit val decoder: ConfDecoder[NumberOfMethodsInTypeCheckerConfig] =
    metaconfig.generic.deriveDecoder(default)
}

class NumberOfMethodsInTypeChecker(config: NumberOfMethodsInTypeCheckerConfig)
    extends SyntacticRule("NumberOfMethodsInTypeChecker") {
  def this() = this(NumberOfMethodsInTypeCheckerConfig.default)

  override def withConfiguration(conf: Configuration): Configured[Rule] =
    conf.conf
      .getOrElse("NumberOfMethodsInTypeChecker")(config)
      .map(new NumberOfMethodsInTypeChecker(_))

  override def fix(implicit doc: SyntacticDocument): Patch =
    NumberOfMethodsInTypeChecker.types(doc.tree).collect {
      case tpe
          if NumberOfMethodsInTypeChecker.countMethods(tpe) > config.maxMethods =>
        Patch.lint(
          NumberOfMethodsInTypeDiagnostic(
            NumberOfMethodsInTypeChecker.namePos(tpe),
            config.maxMethods.toString
          )
        )
    }.asPatch
}

object NumberOfMethodsInTypeChecker {
  private[fix] def types(tree: Tree): List[Tree] =
    tree.collect {
      case d: Defn.Class => d: Tree
      case d: Defn.Trait => d: Tree
      case d: Defn.Object => d: Tree
      case d: Pkg.Object => d: Tree
    }

  private[fix] def countMethods(tree: Tree): Int =
    templateStats(tree).count {
      case _: Defn.Def => true
      case _: scala.meta.Decl.Def => true
      case _ => false
    }

  private def templateStats(tree: Tree): List[Stat] = tree match {
    case d: Defn.Class =>
      d.templ.stats
    case d: Defn.Trait =>
      d.templ.stats
    case d: Defn.Object =>
      d.templ.stats
    case d: Pkg.Object =>
      d.templ.stats
    case _ =>
      Nil
  }

  private[fix] def namePos(tree: Tree): Position = tree match {
    case d: Defn.Class => d.name.pos
    case d: Defn.Trait => d.name.pos
    case d: Defn.Object => d.name.pos
    case d: Pkg.Object => d.name.pos
  }
}

final case class NumberOfMethodsInTypeDiagnostic(
    position: Position,
    maximum: String
) extends Diagnostic {
  override def message: String =
    s"Type contains more than $maximum methods."
}
