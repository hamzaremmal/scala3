// scalac: -Wunused:all

import collection.mutable.{Map => MutMap} // error
import collection.mutable.Set // error

class A {
  import collection.mutable.{Map => MutMap} // OK
  private val a = 1 // error
  val b = 2 // OK

  /* This goes around the trivial method detection */
  val default_int = 12

  val someMap = MutMap()

  private def c1 = 2 // error
  private def c2 = 2 // OK
  def c3 = c2

  def d1(using x:Int): Int = default_int // error
  def d2(using x:Int): Int = x // OK

  def e1(x: Int) = default_int // error
  def e2(x: Int) = x // OK
  def f =
    val x = 1 // error
    def f = 2 // error
    val y = 3 // OK
    def g = 4 // OK
    y + g

  def g(x: Int): Int = x match
    case x:1 => 0 // error
    case x:2 => x // OK
    case _ => 1 // OK
}

/* ---- CHECK scala.annotation.unused ---- */
package foo.test.scala.annotation:
  import annotation.unused // OK

  /* This goes around the trivial method detection */
  val default_int = 12

  def a1(a: Int) = a // OK
  def a2(a: Int) = default_int // error
  def a3(@unused a: Int) = default_int //OK

  def b1 =
    def f = 1 // error
    1

  def b2 =
    def f = 1 // OK
    f

  def b3 =
    @unused def f = 1 // OK
    1

  object Foo:
    private def a = 1 // error
    private def b = 2 // OK
    @unused private def c = 3 // OK

    def other = b
