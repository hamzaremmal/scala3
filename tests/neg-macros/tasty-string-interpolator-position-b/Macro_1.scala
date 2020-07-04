import scala.quoted._
import scala.language.implicitConversions

object Macro {

  extension (strCtx: => StringContext) implicit inline def f3(args: =>Any*): String = ${FIntepolator.apply('strCtx, 'args)}

}

object FIntepolator {
  def apply(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using qctx: QuoteContext) : Expr[String] = {
    import qctx.tasty._
    error("there are no args", argsExpr.unseal.underlyingArgument.pos)
    '{ ($strCtxExpr).s($argsExpr: _*) }
  }

}
