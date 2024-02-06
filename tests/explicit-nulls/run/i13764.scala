import scala.compiletime.uninitialized

class StringFoo:
  var s: String | Null = uninitialized
  override def toString: String = s"Foo:s=$s"

class AnyFoo:
  var a: Any = uninitialized
  override def toString: String = s"Foo:a=$a"

class IntFoo:
  var i: Int = uninitialized
  override def toString: String = s"Foo:i=$i"

class BooleanFoo:
  var b: Boolean = uninitialized
  override def toString: String = s"Foo:b=$b"

object Test:
  def main(args: Array[String]): Unit =
    println(new StringFoo)
    println(new AnyFoo)
    println(new IntFoo)
    println(new BooleanFoo)