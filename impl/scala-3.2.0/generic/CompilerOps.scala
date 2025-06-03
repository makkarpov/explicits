package mx.mk.explicits.sc2
package generic

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.Symbol
import dotty.tools.dotc.core.Types.Type
import dotty.tools.dotc.typer.Implicits.ContextualImplicits
import dotty.tools.dotc.typer.Typer
import dotty.tools.dotc.util.SourcePosition

import scala.quoted.runtime.impl.{ExprImpl, QuotesImpl, SpliceScope, TypeImpl}
import scala.quoted.{Quotes, Expr as QExpr, Type as QType}

private[explicits]
transparent inline def ops(using o: CompilerOps): CompilerOps = o

private[explicits]
trait CompilerOps {
  def createSymbol(name: String, tpe: Type)(using Context): Symbol

  def createHole(idx: Int, tpe: Type)(using Context): tpd.Hole

  extension (c: Context) {
    def freshWithImplicits(ci: ContextualImplicits): Context
  }

  extension (t: Type) {
    def toQuotedType[R](using Context): QType[R]
  }

  extension (e: tpd.Tree) {
    def toQuotedExpr[R](using Context): QExpr[R]
  }

  extension (c: tpd.Tree) {
    def ciType: Type
  }

  extension (t: Typer) {
    def inferImplicit(tpe: Type, pos: SourcePosition)(using Context): tpd.Tree
  }
}
