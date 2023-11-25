package mx.mk.explicits.sc2
package generic

import dotty.tools.dotc.ast.Trees.Untyped
import dotty.tools.dotc.ast.{Trees, tpd, untpd}
import dotty.tools.dotc.core.Contexts.{Context, ctx}
import dotty.tools.dotc.core.Types.Type
import dotty.tools.dotc.printing.{RefinedPrinter, Texts}
import mx.mk.explicits.SearchResult

import scala.quoted.runtime.impl.ExprImpl
import scala.quoted.{Expr, Quotes, Type as QType}

private[explicits]
final class SearchSuccessImpl(holeTypes: Seq[Type], holeTree: tpd.Tree)(using Context, CompilerOps)
extends SearchResult.Success[?] {
  val missingTypes: Seq[QType[?]] = holeTypes.map(_.toQuotedType).toVector

  override def construct(missingValues: Seq[Expr[_]])(using Quotes): Expr[?] = {
    if (missingValues.size != holeTypes.size) {
      throw new IllegalArgumentException("invalid size of missingValues")
    }

    val r = new tpd.TreeMap() {
      override def transform(tree: tpd.Tree)(using Context): tpd.Tree = tree match {
        case h: Trees.Hole[_] => missingValues(h.idx).asInstanceOf[ExprImpl].tree
        case _ => super.transform(tree)
      }
    }.transform(holeTree)

    r.toQuotedExpr
  }

  def show(using Quotes): String = {
    val expr = holeTree
      .toText(new RefinedPrinter(ctx) {
        override protected def toTextCore[T >: Untyped](tree: Trees.Tree[T]): Texts.Text = tree match {
          case h: Trees.Hole[_] => literalText("?(" + holeTypes(h.idx).show + ")")
          case _ => super.toTextCore(tree)
        }
      })
      .mkString(Int.MaxValue, false)

    s"SearchResult.Success($expr)"
  }
}
