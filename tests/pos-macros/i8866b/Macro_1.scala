import scala.quoted._

object Other {
  inline def apply = 5
}

object Macro {

  def impl(using qctx: QuoteContext): Expr[Int] = {
    import qctx.tasty._

    let(
      Select.unique(
        '{ Other }.unseal,
        "apply"
      )
    )(identity).seal.cast[Int]

  }

  inline def apply = ${ Macro.impl }

}
