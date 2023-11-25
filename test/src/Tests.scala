package test

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Tests extends AnyFlatSpec with Matchers {
  it must "resolve plain implicits" in {
    Macros.testPlain[Foo].meow shouldBe "foo"
    Macros.testPlain[Seq[Foo]].meow shouldBe "seq(foo)"
  }

  it must "resolve implicits with givens and extra locs" in {
    Macros.testExtra[Baz].meow shouldBe "baz-given"
    Macros.testExtra[Bar].meow shouldBe "bar-extra"

    Macros.testExtra[Map[Bar, Baz]].meow shouldBe "map(bar-extra, baz-given)"
  }

  it must "resolve assisted implicits" in {
    Macros.testAssisted[Foo].meow shouldBe "foo"

    Macros.testAssisted[Map[Foo, Bar]].meow shouldBe "map(foo, bar-assisted)"
    Macros.testAssisted[Seq[Map[Foo, Bar]]].meow shouldBe "seq(map(foo, bar-assisted))"
    Macros.testAssisted[Triple[Baz, Foo, Bar]].meow shouldBe "triple(baz-assisted, foo, bar-assisted)"
  }
}
