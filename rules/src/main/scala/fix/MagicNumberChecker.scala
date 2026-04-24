package fix

import metaconfig.ConfDecoder
import metaconfig.Configured
import scala.meta.Defn
import scala.meta.Lit
import scala.meta.Position
import scala.meta.Term
import scala.meta.Tree
import scalafix.v1.Configuration
import scalafix.v1.Diagnostic
import scalafix.v1.Patch
import scalafix.v1.Rule
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

final case class MagicNumberCheckerConfig(ignore: String = "-1,0,1,2")

object MagicNumberCheckerConfig {
  val default: MagicNumberCheckerConfig = MagicNumberCheckerConfig()

  implicit val surface: metaconfig.generic.Surface[MagicNumberCheckerConfig] =
    metaconfig.generic.deriveSurface[MagicNumberCheckerConfig]

  implicit val decoder: ConfDecoder[MagicNumberCheckerConfig] =
    metaconfig.generic.deriveDecoder(default)
}

class MagicNumberChecker(config: MagicNumberCheckerConfig)
    extends SyntacticRule("MagicNumberChecker") {
  def this() = this(MagicNumberCheckerConfig.default)

  override def withConfiguration(conf: Configuration): Configured[Rule] =
    conf.conf
      .getOrElse("MagicNumberChecker")(config)
      .map(new MagicNumberChecker(_))

  override def fix(implicit doc: SyntacticDocument): Patch = {
    val ignored = config.ignore.split(",").map(_.trim).filter(_.nonEmpty).toSet
    val excluded = MagicNumberChecker.excludedPositions(doc.tree)

    MagicNumberChecker
      .numericExpressions(doc.tree)
      .collect {
        case numeric
            if !ignored.contains(numeric.normalized) &&
              !excluded.exists(MagicNumberChecker.samePosition(_, numeric.position)) =>
          Patch.lint(MagicNumberDiagnostic(numeric.position))
      }
      .asPatch
  }
}

object MagicNumberChecker {
  private[fix] final case class NumericExpression(
      normalized: String,
      position: Position
  )

  private[fix] def numericExpressions(tree: Tree): List[NumericExpression] = {
    val unary = tree.collect {
      case expr @ Term.ApplyUnary(Term.Name(op), arg)
          if Set("+", "-").contains(op) =>
        toLiteralText(arg).map(text => NumericExpression(s"$op$text", expr.pos))
    }.flatten

    val literals = tree.collect {
      case lit: Lit.Int
          if !insideAny(lit.pos, unary.map(_.position)) =>
        NumericExpression(lit.value.toString, lit.pos)
      case lit: Lit.Long
          if !insideAny(lit.pos, unary.map(_.position)) =>
        NumericExpression(lit.value.toString, lit.pos)
    }

    unary ::: literals
  }

  private[fix] def excludedPositions(tree: Tree): List[Position] = {
    val subtrees: List[Tree] = tree.collect {
      case defn: Defn.Val => List(defn.rhs)
      case param: Term.Param => param.default.toList
    }.flatten

    subtrees.flatMap(collectLiteralPositions)
  }

  private def collectLiteralPositions(subtree: Tree): List[Position] = {
    val unary = subtree.collect {
      case expr @ Term.ApplyUnary(Term.Name(op), arg)
          if Set("+", "-").contains(op) && toLiteralText(arg).isDefined =>
        expr.pos
    }
    val literals = subtree.collect {
      case lit: Lit.Int => lit.pos
      case lit: Lit.Long => lit.pos
    }
    unary ::: literals
  }

  private def toLiteralText(tree: Tree): Option[String] = tree match {
    case lit: Lit.Int => Some(lit.value.toString)
    case lit: Lit.Long => Some(lit.value.toString)
    case _ => None
  }

  private def insideAny(position: Position, outers: List[Position]): Boolean =
    outers.exists(outer => outer.start <= position.start && outer.end >= position.end)

  private def samePosition(left: Position, right: Position): Boolean =
    left.start == right.start && left.end == right.end
}

final case class MagicNumberDiagnostic(position: Position) extends Diagnostic {
  override def message: String = "Magic number."
}
