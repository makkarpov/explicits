package mx.mk.explicits

import impl.GivenImplicit

import scala.quoted.{Expr, Quotes, Type}

object ImplicitSearch {
  /**
   * Create new builder for implicit search
   *
   * @tparam T Type of searched value
   * @return   Builder instance
   */
  def builder[T](using Type[T])(using Quotes): ImplicitSearch[T] =
    new ImplicitSearch[T]
}

/**
 * Mutable builder for the implicit search task
 *
 * @tparam T Type of searched value
 */
final class ImplicitSearch[T] private (using searchType: Type[T], quotes: Quotes) {
  private var _locations: Seq[Symbol] = Vector.empty
  private var _givens: Seq[GivenImplicit[?]] = Vector.empty
  private var _assisted: Option[Type[?] => Boolean] = None

  /**
   * Add extra search locations. Effect of calling this method is similar to having `import ${loc}.*` around the
   * implicit lookup point.
   *
   * @param locs Additional search locations
   */
  def extraLocations(locs: Symbol*): this.type = {
    _locations ++= locs
    this
  }

  /**
   * Provide a given instance for the type `R`. Provided expression will be inlined to all places where corresponding
   * implicit is used.
   *
   * @param value Expression to be given
   */
  def give[R](value: Expr[R])(using Type[R]): this.type = {
    _givens :+= GivenImplicit(Type.of[R], value)
    this
  }

  /**
   * Enable assisted implicit resolution.
   *
   * Filter function is used to narrow the scope of possible types. Assisted resolution is used as the last resort
   * only when `searchLocations` and `give`ns yield nothing.
   *
   * @param filter Type filter function
   */
  def assist(filter: Type[?] => Boolean): this.type = {
    _assisted = Some(filter)
    this
  }

  /**
   * Perform actual implicit search with configured settings.
   *
   * @return Search result
   */
  def search(): SearchResult[T] =
    CompilerBridge.instance.search(searchType, _givens, _locations, _assisted.getOrElse(_ => false))
}
