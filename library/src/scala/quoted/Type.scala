package scala.quoted

import scala.quoted.show.SyntaxHighlight

/** Quoted type (or kind) `T` */
class Type[T <: AnyKind] private[scala] {
  type `$splice` = T

  /** Show a source code like representation of this type without syntax highlight */
  def show(using qctx: QuoteContext): String = qctx.show(this, SyntaxHighlight.plain)

  /** Show a source code like representation of this type */
  def show(syntaxHighlight: SyntaxHighlight)(using qctx: QuoteContext): String = qctx.show(this, syntaxHighlight)

}

/** Some basic type tags, currently incomplete */
object Type {

  given UnitTag(using qctx: QuoteContext): Type[Unit] = {
    import qctx.tasty.{_, given}
    defn.UnitType.seal.asInstanceOf[quoted.Type[Unit]]
  }

  given BooleanTag(using qctx: QuoteContext): Type[Boolean] = {
    import qctx.tasty.{_, given}
    defn.BooleanType.seal.asInstanceOf[quoted.Type[Boolean]]
  }

  given ByteTag(using qctx: QuoteContext): Type[Byte] = {
    import qctx.tasty.{_, given}
    defn.ByteType.seal.asInstanceOf[quoted.Type[Byte]]
  }

  given CharTag(using qctx: QuoteContext): Type[Char] = {
    import qctx.tasty.{_, given}
    defn.CharType.seal.asInstanceOf[quoted.Type[Char]]
  }

  given ShortTag(using qctx: QuoteContext): Type[Short] = {
    import qctx.tasty.{_, given}
    defn.ShortType.seal.asInstanceOf[quoted.Type[Short]]
  }

  given IntTag(using qctx: QuoteContext): Type[Int] = {
    import qctx.tasty.{_, given}
    defn.IntType.seal.asInstanceOf[quoted.Type[Int]]
  }

  given LongTag(using qctx: QuoteContext): Type[Long] = {
    import qctx.tasty.{_, given}
    defn.LongType.seal.asInstanceOf[quoted.Type[Long]]
  }

  given FloatTag(using qctx: QuoteContext): Type[Float] = {
    import qctx.tasty.{_, given}
    defn.FloatType.seal.asInstanceOf[quoted.Type[Float]]
  }

  given DoubleTag(using qctx: QuoteContext): Type[Double] = {
    import qctx.tasty.{_, given}
    defn.DoubleType.seal.asInstanceOf[quoted.Type[Double]]
  }

}
