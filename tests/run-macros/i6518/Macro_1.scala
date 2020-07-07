import scala.quoted._

object Macros {

  inline def test(): String = ${ testImpl }

  private def testImpl(using qctx: QuoteContext) : Expr[String] = {
    import qctx.tasty._
    val classSym = typeOf[Function1[_, _]].classSymbol.get
    classSym.classMethod("apply")
    classSym.classMethods
    classSym.method("apply")
    Expr(classSym.methods.map(_.name).sorted.mkString("\n"))
  }

}
