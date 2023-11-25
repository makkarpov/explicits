package mx.mk.explicits.sc2
package generic

import mx.mk.explicits.impl.GivenImplicit
import mx.mk.explicits.SearchResult
import dotty.tools.dotc.ast.Trees.{Tree, Untyped}
import dotty.tools.dotc.ast.{Trees, tpd, untpd}
import dotty.tools.dotc.core.*
import dotty.tools.dotc.core.Contexts.{Context, ctx, inContext}
import dotty.tools.dotc.core.Symbols.Symbol
import dotty.tools.dotc.core.Types.{ImplicitRef, Type}
import dotty.tools.dotc.printing.RefinedPrinter
import dotty.tools.dotc.printing.Texts.Text
import dotty.tools.dotc.typer.Implicits.{ContextualImplicits, NoMatchingImplicitsFailure}
import dotty.tools.dotc.typer.{Implicits, ImportInfo}
import dotty.tools.dotc.util.{SourcePosition, Spans}

import mx.mk.explicits.SearchResult

import scala.annotation.tailrec
import scala.quoted.runtime.impl.{ExprImpl, QuotesImpl, SpliceScope, TypeImpl}
import scala.quoted.{Quotes, Expr as QExpr, Type as QType}

private[explicits]
final class SearchContext(
  targetType:     Type,
  extraGivens:    Seq[(Type, tpd.Tree)],
  extraLocations: Seq[Symbol],
  assistedFilter: Type => Boolean,
  position:       SourcePosition
)(using Context, CompilerOps) {
  val extraGivenSymbols: Vector[Symbol] = extraGivens
    .zipWithIndex
    .map { case ((t, _), i) => ops.createSymbol("$extraGiven$" + i, t) }
    .toVector

  var placeholderTypes: Vector[Type] = Vector.empty
  var placeholderSyms: Map[Symbol, Int] = Map.empty
  var placeholderRefs: List[ImplicitRef] = Nil

  /** Context with all 'fixed' fields (extra imports and givens) populated */
  val fixedContext: Context = computeFixedContext()

  def computeFixedContext(): Context = {
    val imports: Seq[ImportInfo] = extraLocations
      .map { sym =>
        new ImportInfo(
          Symbols.newImportSymbol(ctx.owner, tpd.Ident(sym.namedType), Spans.NoCoord),
          List(untpd.ImportSelector(untpd.Ident(StdNames.nme.EMPTY))),
          untpd.EmptyTree, isRootImport = false
        )
      }

    val importCtx: Context = imports.foldLeft(ctx)(_.fresh.setImportInfo(_))

    if (extraGivens.isEmpty) importCtx
    else inContext(importCtx) {
      val refs = extraGivenSymbols.map(s => Types.TermRef(Types.NoPrefix, s)).toList
      ctx.freshWithImplicits(new ContextualImplicits(refs, ctx.implicits, false)(ctx))
    }
  }

  def addPlaceholder(tpe: Type): Unit = {
    val index = placeholderTypes.size
    val sym = ops.createSymbol("$assisted$" + index, tpe)

    placeholderTypes :+= tpe
    placeholderSyms += sym -> index
    placeholderRefs = Types.TermRef(Types.NoPrefix, sym) :: placeholderRefs
  }

  def searchImplicits(): tpd.Tree = {
    val searchContext: Context =
      if (placeholderRefs.isEmpty) fixedContext
      else fixedContext.freshWithImplicits(new ContextualImplicits(placeholderRefs, fixedContext.implicits,
        isImport = false)(fixedContext))

    inContext(searchContext) {
      ctx.typer.inferImplicitArg(targetType, position.span)
    }
  }

  def findFailureNode(t: tpd.Tree): Option[tpd.SearchFailureIdent] = {
    var failureNode: Option[tpd.SearchFailureIdent] = None

    new tpd.TreeTraverser {
      def traverse(tree: tpd.Tree)(using Context): Unit = tree match {
        case t: tpd.SearchFailureIdent =>
          if (failureNode.isEmpty) failureNode = Some(t)

        case _ => if (failureNode.isEmpty) traverseChildren(tree)
      }
    }.traverse(t)

    failureNode
  }

  def makeFailure(result: tpd.Tree): SearchResult.Failure =
    result.ciType match {
      case f: Implicits.SearchFailureType => new SearchFailureImpl(f.explanation)
      case _ => throw new RuntimeException("Unexpected implicit lookup failure: " + result.show)
    }

  def extractFailedType(failureNode: tpd.Tree): Option[Type] = {
    Some(failureNode)
      .map(_.ciType).filter(_.isInstanceOf[Implicits.NoMatchingImplicits])
      .map(_.asInstanceOf[Implicits.NoMatchingImplicits].expectedType)
  }

  def finalizeTree(result: tpd.Tree): tpd.Tree = {
    val extraGivenMap = extraGivenSymbols
      .zipWithIndex
      .map { case (s, i) => s -> extraGivens(i)._2 }
      .toMap

    new tpd.TreeMap {
      override def transform(tree: tpd.Tree)(using Context): tpd.Tree = tree match {
        case t: tpd.Ident =>
          val sym = t.symbol
          if (extraGivenMap.contains(sym)) extraGivenMap(sym)
          else if (placeholderSyms.contains(sym)) {
            val idx = placeholderSyms(sym)
            ops.createHole(idx, placeholderTypes(idx))
          } else super.transform(tree)

        case _ => super.transform(tree)
      }
    }.transform(result)
  }

  def makeSuccess(result: tpd.Tree): SearchResult.Success[?] =
    new SearchSuccessImpl(placeholderTypes, result)

  def result: SearchResult[?] = {
    var result = searchImplicits()
    var failureNode = findFailureNode(result)

    while (failureNode.isDefined) {
      val fn: tpd.Tree = failureNode.get
      if (fn == result || !fn.ciType.isInstanceOf[Implicits.NoMatchingImplicits]) {
        return makeFailure(fn)
      }

      extractFailedType(fn) match {
        case Some(tpe) if assistedFilter(tpe) =>
          addPlaceholder(tpe)

          result = searchImplicits()
          failureNode = findFailureNode(result)

        case _ => return makeFailure(fn)
      }
    }

    makeSuccess(finalizeTree(result))
  }
}
