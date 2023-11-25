package mx.mk.explicits.sc3

import dotty.tools.dotc.core.Contexts.{Context, ContextBase, FreshContext}
import dotty.tools.dotc.typer.Implicits
import dotty.tools.dotc.typer.Implicits.ContextualImplicits

private[explicits]
class ImplicitContext(outer: Context, override val implicits: ContextualImplicits)
extends FreshContext(outer.base) {
  reuseIn(outer)
  setTyperState(outer.typerState)
}
