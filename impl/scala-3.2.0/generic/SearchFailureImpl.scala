package mx.mk.explicits.sc2
package generic

import mx.mk.explicits.SearchResult

import scala.quoted.Quotes

private[explicits]
final class SearchFailureImpl(val explanation: String) extends SearchResult.Failure {
  def show(using Quotes): String = s"SearchResult.Failure('$explanation')"
}
