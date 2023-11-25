package mx.mk.explicits

import scala.quoted.Quotes

object Symbol {
  /** Lookup a symbol for module, using the module name */
  def forModule(s: String)(using q: Quotes): Symbol = {
    import q.reflect.Symbol as QS
    QS.requiredModule(s)
  }

  /** Wrap a symbol from quotes into a path-independent symbol object */
  given fromQuotes(using q: Quotes): Conversion[q.reflect.Symbol, Symbol] =
    s => new Symbol(s)
}

/** A wrapper for `(q: Quotes).reflect.Symbol` that is not path-dependent */
final class Symbol private(inner: AnyRef) {
  /** Unwrap a path-independent symbol back into a quotes object */
  inline def toSymbol(using q: Quotes): q.reflect.Symbol = inner.asInstanceOf[q.reflect.Symbol]
}
