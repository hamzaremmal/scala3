package a
package b

object Givens/*<-a::b::Givens.*/:

  extension on [A](any: A):
    def sayHel/*<-a::b::Givens.extension_extension_sayHello_A.*//*<-a::b::Givens.extension_extension_sayHello_A.extension_sayHello().[A]*//*<-a::b::Givens.extension_extension_sayHello_A.extension_sayHello().(any)*//*->a::b::Givens.extension_extension_sayHello_A.extension_sayHello().[A]*/lo = s"Hello/*<-a::b::Givens.extension_extension_sayHello_A.extension_sayHello().*//*->scala::StringContext.apply().*/, I am $any/*->a::b::Givens.extension_extension_sayHello_A.extension_sayHello().(any)*/"/*->scala::StringContext#s().*/

  extension on [B](any: B):
    def sayGoodb/*<-a::b::Givens.extension_extension_sayGoodbye_B.*//*<-a::b::Givens.extension_extension_sayGoodbye_B.extension_sayGoodbye().[B]*//*<-a::b::Givens.extension_extension_sayGoodbye_B.extension_saySoLong().[B]*//*<-a::b::Givens.extension_extension_sayGoodbye_B.extension_sayGoodbye().(any)*//*<-a::b::Givens.extension_extension_sayGoodbye_B.extension_saySoLong().(any)*//*->a::b::Givens.extension_extension_sayGoodbye_B.extension_sayGoodbye().[B]*//*->a::b::Givens.extension_extension_sayGoodbye_B.extension_saySoLong().[B]*/ye = s"Goodb/*<-a::b::Givens.extension_extension_sayGoodbye_B.extension_sayGoodbye().*//*->scala::StringContext.apply().*/ye, from $any/*->a::b::Givens.extension_extension_sayGoodbye_B.extension_sayGoodbye().(any)*/"/*->scala::StringContext#s().*/
    def saySoLong = s"So Lo/*<-a::b::Givens.extension_extension_sayGoodbye_B.extension_saySoLong().*//*->scala::StringContext.apply().*/ng, from $any/*->a::b::Givens.extension_extension_sayGoodbye_B.extension_saySoLong().(any)*/"/*->scala::StringContext#s().*/

  val hello1/*<-a::b::Givens.hello1.*/ = /*->a::b::Givens.extension_extension_sayHello_A.extension_sayHello().*/1.sayHello
  val goodbye1/*<-a::b::Givens.goodbye1.*/ = /*->a::b::Givens.extension_extension_sayGoodbye_B.extension_sayGoodbye().*/1.sayGoodbye
  val soLong1/*<-a::b::Givens.soLong1.*/ = /*->a::b::Givens.extension_extension_sayGoodbye_B.extension_saySoLong().*/1.saySoLong

  trait Monoid/*<-a::b::Givens.Monoid#*/[A/*<-a::b::Givens.Monoid#[A]*/]:
    def empty/*<-a::b::Givens.Monoid#empty().*/: A/*->a::b::Givens.Monoid#[A]*/
    extension (x/*<-a::b::Givens.Monoid#extension_combine().(x)*/: A/*->a::b::Givens.Monoid#[A]*/) def combine(y: A): A
/*<-a::b::Givens.Monoid#extension_combine().*//*<-a::b::Givens.Monoid#extension_combine().(y)*//*->a::b::Givens.Monoid#[A]*//*->a::b::Givens.Monoid#[A]*/
  given Monoid[String]:
   /*<-a::b::Givens.given_Monoid_String.*//*->a::b::Givens.Monoid#*//*->scala::Predef.String#*/ def empty/*<-a::b::Givens.given_Monoid_String.empty().*/ = ""
    extension (x/*<-a::b::Givens.given_Monoid_String.extension_combine().(x)*/: String/*->scala::Predef.String#*/) def combine(y: String/*<-a::b::Givens.given_Monoid_String.extension_combine().*//*<-a::b::Givens.given_Monoid_String.extension_combine().(y)*//*->scala::Predef.String#*/) = x/*->a::b::Givens.given_Monoid_String.extension_combine().(x)*/ +/*->java::lang::String#`+`().*/ y/*->a::b::Givens.given_Monoid_String.extension_combine().(y)*/

  inline given int2String/*<-a::b::Givens.int2String().*/ as Conversion/*->scala::Conversion#*/[Int/*->scala::Int#*/, String/*->scala::Predef.String#*/] = _.toString/*->scala::Any#toString().*/

  def foo/*<-a::b::Givens.foo().*/[A/*<-a::b::Givens.foo().[A]*/](using A/*<-a::b::Givens.foo().(A)*/: Monoid/*->a::b::Givens.Monoid#*/[A/*->a::b::Givens.foo().[A]*/]): A/*->a::b::Givens.foo().[A]*/ = A/*->a::b::Givens.foo().(A)*/.extension_combine/*->a::b::Givens.Monoid#extension_combine().*/(A/*->a::b::Givens.foo().(A)*/.empty/*->a::b::Givens.Monoid#empty().*/)(A/*->a::b::Givens.foo().(A)*/.empty/*->a::b::Givens.Monoid#empty().*/)
