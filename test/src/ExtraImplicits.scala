package test

object ExtraImplicits {
  given barCanMeow: CanMeow[Bar] with {
    def meow: String = "bar-extra"
  }
}
