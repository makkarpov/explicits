package test

trait CanMeow[T] {
  def meow: String
}

object CanMeow {
  given seqCanMeow[T](using v: CanMeow[T]): CanMeow[Seq[T]] with {
    def meow: String = s"seq(${v.meow})"
  }

  given mapCanMeow[K, V](using k: CanMeow[K], v: CanMeow[V]): CanMeow[Map[K, V]] with {
    def meow: String = s"map(${k.meow}, ${v.meow})"
  }

  given fooCanMeow: CanMeow[Foo] with {
    def meow: String = s"foo"
  }

  given tripleCanMeow[A, B, C](using a: CanMeow[A], b: CanMeow[B], c: CanMeow[C]): CanMeow[Triple[A, B, C]] with {
    def meow: String = s"triple(${a.meow}, ${b.meow}, ${c.meow})"
  }
}
