import scala.quoted._


object Macros {

  inline def swapFandG(x: => Unit): Unit = ${impl('x)}

  private def impl(x: Expr[Unit])(using QuoteContext): Expr[Unit] = {
    x match {
      case '{ DSL.f[$T]($x) } => '{ DSL.g[T]($x) }
      case '{ DSL.g[$T]($x) } => '{ DSL.f[T]($x) }
      case _ => x
    }
  }

}

//
// DSL in which the user write the code
//

object DSL {
  def f[T](x: T): Unit = println("f: " + x.toString)
  def g[T](x: T): Unit = println("g: " + x.toString)
}
