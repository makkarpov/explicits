package mx.mk.explicits.sc2

import mx.mk.explicits.{CompilerBridge, SearchResult, Symbol}
import mx.mk.explicits.impl.GivenImplicit
import dotty.tools.dotc.core.Contexts.Context
import mx.mk.explicits.sc2.generic.{AbstractBridge, CompilerOps}

import scala.annotation.tailrec
import scala.quoted.runtime.impl.{ExprImpl, QuotesImpl, SpliceScope, TypeImpl}
import scala.quoted.{Expr, Quotes, Type as QType}

private[explicits]
class BridgeImpl extends AbstractBridge {
  protected def createOps(): CompilerOps = new CompilerOpsImpl
}
