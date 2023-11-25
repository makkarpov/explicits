package mx.mk.explicits.sc3

import dotty.tools.dotc.ast.{Trees, tpd, untpd}
import dotty.tools.dotc.core.Contexts.{Context, ctx}
import dotty.tools.dotc.core.Symbols.Symbol
import dotty.tools.dotc.core.Types.Type
import dotty.tools.dotc.core.{Flags, Names, Symbols}
import dotty.tools.dotc.typer.Implicits.ContextualImplicits
import mx.mk.explicits.sc2.generic.CompilerOps

import scala.quoted.runtime.impl.{ExprImpl, SpliceScope, TypeImpl}
import scala.quoted.{Expr as QExpr, Type as QType}

// Source code is identical to Scala 3.2.0 version, but dotty has incompatible changes in binary API
// noinspection DuplicatedCode
private[explicits]
class CompilerOpsImpl extends CompilerOps {
  def createSymbol(name: String, tpe: Type)(using Context): Symbol =
    Symbols.newSymbol(ctx.owner, Names.termName(name), Flags.EmptyFlags, tpe)

  def createHole(idx: Int, tpe: Type)(using Context): tpd.Hole =
    tpd.Hole(true, idx, Nil, tpd.EmptyTree, tpd.TypeTree(tpe))

  extension (c: Context) {
    def freshWithImplicits(ci: ContextualImplicits): Context =
      new ImplicitContext(c, ci)
  }

  extension (t: Type) {
    def toQuotedType[R](using Context): QType[R] =
      new TypeImpl(tpd.TypeTree(t), SpliceScope.getCurrent).asInstanceOf[QType[R]]
  }

  extension (e: tpd.Tree) {
    def toQuotedExpr[R](using Context): QExpr[R] =
      new ExprImpl(e, SpliceScope.getCurrent).asInstanceOf[QExpr[R]]
  }

  extension (c: tpd.Tree) {
    def ciType: Type = c.tpe
  }
}
