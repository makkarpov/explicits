package mx.mk.explicits.sc2
package generic

import mx.mk.explicits.{CompilerBridge, SearchResult, Symbol}
import mx.mk.explicits.impl.GivenImplicit
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols

import scala.annotation.tailrec
import scala.quoted.runtime.impl.{ExprImpl, QuotesImpl, SpliceScope, TypeImpl}
import scala.quoted.{Expr, Quotes, Type as QType}

private[explicits]
abstract class AbstractBridge extends CompilerBridge {
  override final def search[T](using q: Quotes)(
    targetType:     QType[T],
    extraGivens:    Seq[GivenImplicit[_]],
    extraLocations: Seq[Symbol],
    assistedFilter: QType[_] => Boolean
  ): SearchResult[T] = {
    val qi: QuotesImpl & q.type = q.asInstanceOf[QuotesImpl & q.type]

    import qi.ctx
    import qi.reflect.*

    given ops: CompilerOps = createOps()

    new SearchContext(
      TypeRepr.of(using targetType),
      extraGivens.map(g => TypeRepr.of(using g.tpe) -> g.expr.asTerm),
      extraLocations.map(_.toSymbol.asInstanceOf[Symbols.Symbol]),
      tpe => assistedFilter(tpe.toQuotedType),
      Position.ofMacroExpansion
    ).result.asInstanceOf[SearchResult[T]]
  }

  protected def createOps(): CompilerOps
}
