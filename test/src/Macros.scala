package test

import mx.mk.explicits.{ImplicitSearch, SearchResult, Symbol}

import scala.quoted.{Expr, Quotes, Type, quotes}

object Macros {
  inline def testPlain[T]: CanMeow[T] = ${ testPlainImpl[T] }

  inline def testExtra[T]: CanMeow[T] = ${ testExtraImpl[T] }

  inline def testAssisted[T]: CanMeow[T] = ${ testAssistedImpl[T] }

  private def testPlainImpl[T](using Quotes, Type[T]): Expr[CanMeow[T]] =
    ImplicitSearch.builder[CanMeow[T]]
      .search()
      .toSuccess.construct(Nil)
      .asExprOf[CanMeow[T]]

  private def testExtraImpl[T](using Quotes, Type[T]): Expr[CanMeow[T]] =
    ImplicitSearch.builder[CanMeow[T]]
      .give[CanMeow[Baz]](literalMeow("baz-given"))
      .extraLocations(Symbol.forModule("test.ExtraImplicits"))
      .search()
      .toSuccess.construct(Nil)
      .asExprOf[CanMeow[T]]

  private def testAssistedImpl[T](using Quotes, Type[T]): Expr[CanMeow[T]] = {
    val r = ImplicitSearch.builder[CanMeow[T]]
      .assist {
        case '[ CanMeow[t] ] => true
        case _ => false
      }
      .search()
      .toSuccess

    val assistedValues = r
      .missingTypes
      .map {
        case '[ CanMeow[t] ] =>
          val simpleName = Type.show[t].split("\\.").last.toLowerCase
          literalMeow[t](simpleName + "-assisted")
      }

    r.construct(assistedValues)
  }

  private def literalMeow[T](message: String)(using Quotes, Type[T]): Expr[CanMeow[T]] =
    '{ new CanMeow[T] { def meow: String = ${ Expr(message) } } }
}
