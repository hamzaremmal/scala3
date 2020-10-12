import scala.quoted._

object OtherMacro {

  def impl(using qctx: QuoteContext): Expr[Int] =
    '{ 42 }

  inline def apply = ${ OtherMacro.impl }

}

object Macro {

  def impl(using qctx: QuoteContext): Expr[Int] = {
    import qctx.reflect._

    let(
      Select.unique(
        '{ OtherMacro }.unseal,
        "apply"
      )
    )(identity).seal.cast[Int]
  }

  inline def apply = ${ Macro.impl }

}
