package mx.mk.explicits.sc3

import dotty.tools.dotc.core.Contexts.Context
import mx.mk.explicits.impl.GivenImplicit
import mx.mk.explicits.sc2.generic.{AbstractBridge, CompilerOps}
import mx.mk.explicits.{CompilerBridge, SearchResult, Symbol}

import scala.annotation.tailrec
import scala.quoted.runtime.impl.{ExprImpl, QuotesImpl, SpliceScope, TypeImpl}
import scala.quoted.{Expr, Quotes, Type as QType}

private[explicits]
class BridgeImpl extends AbstractBridge {
  protected def createOps(): CompilerOps = new CompilerOpsImpl
}
