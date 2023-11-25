package mx.mk.explicits

import scala.quoted.{Expr, Quotes, Type, quotes}

object SearchResult {
  trait Success[+T] extends SearchResult[T] {
    /** @return Types of the missing values */
    def missingTypes: Seq[Type[?]]

    /** Construct a final resolved expression, given the substitutions for missing values */
    def construct(missingValues: Seq[Expr[?]])(using Quotes): Expr[T]

    override def toSuccess(using Quotes): Success[T] = this
  }

  trait Failure extends SearchResult[Nothing] {
    /** Human-readable explanation for failure */
    def explanation: String

    override def toSuccess(using Quotes): Success[Nothing] = quotes.reflect.report.errorAndAbort(explanation)
  }
}

sealed trait SearchResult[+T] {
  /** Transforms result to success, or aborts the macros with an error */
  def toSuccess(using Quotes): SearchResult.Success[T]

  def show(using Quotes): String
}
