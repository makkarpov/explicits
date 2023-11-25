package mx.mk.explicits
package impl

import scala.quoted.{Type, Expr}

private[explicits]
case class GivenImplicit[T](tpe: Type[T], expr: Expr[T])
