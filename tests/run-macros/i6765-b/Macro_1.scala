import scala.quoted._

inline def foo = ${fooImpl}

def fooImpl(using qctx: QuoteContext) = {
  val res = Expr.ofList(List('{"One"}))
  Expr(res.show)
}
