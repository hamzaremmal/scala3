import scala.quoted._
class Foo {
  def test with QuoteContext = '{
    ${3} // error
    ${new Object} // error
    ${"abc"} // error
    ${()} // error
    ${new Foo} // error
  }

  def unary_$ : Int = 9
}
