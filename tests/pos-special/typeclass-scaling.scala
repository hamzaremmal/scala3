import scala.collection.mutable
import scala.annotation.tailrec

// The following command:
//
//     sc typeclass-scaling.scala -Xmax-inlines 100 -Xprint:front -color:never -Yshow-no-inline -pagewidth 1000 >& x
//
// produces an output file with `wc` measuures (lines/words/chars):
//
//     41030  100905 3862435
//
// The command
//
//     time sc typeclass-scaling.scala -Xmax-inlines 100
//
// gives (best of three):
//
// real	0m14.322s
// user	0m55.402s
// sys	0m1.110s
object datatypes {
  import typeclasses._

  enum E1[T] derives Eq, Pickler {
    case C1(x1: T)
  }

  enum E2[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
  }

  enum E3[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
  }

  enum E4[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
  }

  enum E5[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
    case C5(x1: T, x2: T, x3: T, x4: T, x5: T)
  }

  enum E6[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
    case C5(x1: T, x2: T, x3: T, x4: T, x5: T)
    case C6(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T)
  }

  enum E7[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
    case C5(x1: T, x2: T, x3: T, x4: T, x5: T)
    case C6(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T)
    case C7(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T)
  }

  enum E8[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
    case C5(x1: T, x2: T, x3: T, x4: T, x5: T)
    case C6(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T)
    case C7(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T)
    case C8(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T)
  }

  enum E9[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
    case C5(x1: T, x2: T, x3: T, x4: T, x5: T)
    case C6(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T)
    case C7(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T)
    case C8(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T)
    case C9(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T)
  }

  enum E10[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
    case C5(x1: T, x2: T, x3: T, x4: T, x5: T)
    case C6(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T)
    case C7(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T)
    case C8(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T)
    case C9(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T)
    case C10(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T)
  }

  enum E11[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
    case C5(x1: T, x2: T, x3: T, x4: T, x5: T)
    case C6(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T)
    case C7(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T)
    case C8(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T)
    case C9(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T)
    case C10(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T)
    case C11(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T)
  }

  enum E12[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
    case C5(x1: T, x2: T, x3: T, x4: T, x5: T)
    case C6(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T)
    case C7(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T)
    case C8(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T)
    case C9(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T)
    case C10(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T)
    case C11(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T)
    case C12(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T)
  }

  enum E13[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
    case C5(x1: T, x2: T, x3: T, x4: T, x5: T)
    case C6(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T)
    case C7(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T)
    case C8(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T)
    case C9(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T)
    case C10(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T)
    case C11(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T)
    case C12(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T)
    case C13(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T, x13: T)
  }

  enum E14[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
    case C5(x1: T, x2: T, x3: T, x4: T, x5: T)
    case C6(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T)
    case C7(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T)
    case C8(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T)
    case C9(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T)
    case C10(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T)
    case C11(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T)
    case C12(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T)
    case C13(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T, x13: T)
    case C14(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T, x13: T, x14: T)
  }

  enum E15[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
    case C5(x1: T, x2: T, x3: T, x4: T, x5: T)
    case C6(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T)
    case C7(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T)
    case C8(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T)
    case C9(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T)
    case C10(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T)
    case C11(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T)
    case C12(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T)
    case C13(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T, x13: T)
    case C14(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T, x13: T, x14: T)
    case C15(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T, x13: T, x14: T, x15: T)
  }

  enum E16[T] derives Eq, Pickler {
    case C1(x1: T)
    case C2(x1: T, x2: T)
    case C3(x1: T, x2: T, x3: T)
    case C4(x1: T, x2: T, x3: T, x4: T)
    case C5(x1: T, x2: T, x3: T, x4: T, x5: T)
    case C6(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T)
    case C7(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T)
    case C8(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T)
    case C9(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T)
    case C10(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T)
    case C11(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T)
    case C12(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T)
    case C13(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T, x13: T)
    case C14(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T, x13: T, x14: T)
    case C15(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T, x13: T, x14: T, x15: T)
    case C16(x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T, x13: T, x14: T, x15: T, x16: T)
  }
}

object typeclasses {
  // A typeclass
  trait Eq[T] {
    def eql(x: T, y: T): Boolean
  }

  object Eq {
    import scala.typelevel.erasedValue
    import typelevel._

    inline def tryEql[T](x: T, y: T) = implicit match {
      case eq: Eq[T] => eq.eql(x, y)
    }

    inline def eqlElems[Elems <: Tuple](xs: Mirror, ys: Mirror, n: Int): Boolean =
      inline erasedValue[Elems] match {
        case _: (elem *: elems1) =>
          tryEql[elem](xs(n).asInstanceOf, ys(n).asInstanceOf) &&
          eqlElems[elems1](xs, ys, n + 1)
        case _: Unit =>
          true
      }

    inline def eqlCase[T, Elems <: Tuple](r: Reflected[T], x: T, y: T) =
      eqlElems[Elems](r.reflect(x), r.reflect(y), 0)

    inline def eqlCases[T, Alts <: Tuple](r: Reflected[T], x: T, y: T): Boolean =
      inline erasedValue[Alts] match {
        case _: (Shape.Case[alt, elems] *: alts1) =>
          x match {
            case x: `alt` =>
              y match {
                case y: `alt` => eqlCase[T, elems](r, x, y)
                case _ => false
              }
            case _ => eqlCases[T, alts1](r, x, y)
        }
      case _: Unit =>
        false
    }

    inline def derived[T, S <: Shape](implicit ev: Shaped[T, S]): Eq[T] = new {
      def eql(x: T, y: T): Boolean = inline erasedValue[S] match {
        case _: Shape.Cases[alts] =>
          eqlCases[T, alts](ev, x, y)
        case _: Shape.Case[_, elems] =>
          eqlCase[T, elems](ev, x, y)
      }
    }

    implicit object IntEq extends Eq[Int] {
      def eql(x: Int, y: Int) = x == y
    }
  }

  // Another typeclass
  trait Pickler[T] {
    def pickle(buf: mutable.ListBuffer[Int], x: T): Unit
    def unpickle(buf: mutable.ListBuffer[Int]): T
  }

  object Pickler {
    import scala.typelevel.{erasedValue, constValue}
    import typelevel._

    def nextInt(buf: mutable.ListBuffer[Int]): Int = try buf.head finally buf.trimStart(1)

    inline def tryPickle[T](buf: mutable.ListBuffer[Int], x: T): Unit = implicit match {
      case pkl: Pickler[T] => pkl.pickle(buf, x)
    }

    inline def pickleElems[Elems <: Tuple](buf: mutable.ListBuffer[Int], elems: Mirror, n: Int): Unit =
      inline erasedValue[Elems] match {
        case _: (elem *: elems1) =>
          tryPickle[elem](buf, elems(n).asInstanceOf[elem])
          pickleElems[elems1](buf, elems, n + 1)
        case _: Unit =>
      }

    inline def pickleCase[T, Elems <: Tuple](r: Reflected[T], buf: mutable.ListBuffer[Int], x: T): Unit =
      pickleElems[Elems](buf, r.reflect(x), 0)

    inline def pickleCases[T, Alts <: Tuple](r: Reflected[T], buf: mutable.ListBuffer[Int], x: T, n: Int): Unit =
      inline erasedValue[Alts] match {
        case _: (Shape.Case[alt, elems] *: alts1) =>
          x match {
            case x: `alt` =>
              buf += n
              pickleCase[T, elems](r, buf, x)
            case _ =>
              pickleCases[T, alts1](r, buf, x, n + 1)
          }
        case _: Unit =>
      }

    inline def tryUnpickle[T](buf: mutable.ListBuffer[Int]): T = implicit match {
      case pkl: Pickler[T] => pkl.unpickle(buf)
    }

    inline def unpickleElems[Elems <: Tuple](buf: mutable.ListBuffer[Int], elems: Array[AnyRef], n: Int): Unit =
      inline erasedValue[Elems] match {
        case _: (elem *: elems1) =>
          elems(n) = tryUnpickle[elem](buf).asInstanceOf[AnyRef]
          unpickleElems[elems1](buf, elems, n + 1)
        case _: Unit =>
      }

    inline def unpickleCase[T, Elems <: Tuple](r: Reflected[T], buf: mutable.ListBuffer[Int], ordinal: Int): T = {
      inline val size = constValue[Tuple.Size[Elems]]
      inline if (size == 0)
        r.reify(r.common.mirror(ordinal))
      else {
        val elems = new Array[Object](size)
        unpickleElems[Elems](buf, elems, 0)
        r.reify(r.common.mirror(ordinal, elems))
      }
    }

    inline def unpickleCases[T, Alts <: Tuple](r: Reflected[T], buf: mutable.ListBuffer[Int], ordinal: Int, n: Int): T =
      inline erasedValue[Alts] match {
        case _: (Shape.Case[_, elems] *: alts1) =>
          if (n == ordinal) unpickleCase[T, elems](r, buf, ordinal)
          else unpickleCases[T, alts1](r, buf, ordinal, n + 1)
        case _ =>
          throw new IndexOutOfBoundsException(s"unexpected ordinal number: $ordinal")
      }

    inline def derived[T, S <: Shape](implicit ev: Shaped[T, S]): Pickler[T] = new {
      def pickle(buf: mutable.ListBuffer[Int], x: T): Unit = inline erasedValue[S] match {
        case _: Shape.Cases[alts] =>
          pickleCases[T, alts](ev, buf, x, 0)
        case _: Shape.Case[_, elems] =>
          pickleCase[T, elems](ev, buf, x)
      }
      def unpickle(buf: mutable.ListBuffer[Int]): T = inline erasedValue[S] match {
        case _: Shape.Cases[alts] =>
          unpickleCases[T, alts](ev, buf, nextInt(buf), 0)
        case _: Shape.Case[_, elems] =>
          unpickleCase[T, elems](ev, buf, 0)
      }
    }

    implicit object IntPickler extends Pickler[Int] {
      def pickle(buf: mutable.ListBuffer[Int], x: Int): Unit = buf += x
      def unpickle(buf: mutable.ListBuffer[Int]): Int = nextInt(buf)
    }
  }
}
import datatypes._
import typeclasses._

// Tests
object Test extends App {
  implicitly[Eq[E1[Int]]]
  implicitly[Eq[E2[Int]]]
  implicitly[Eq[E3[Int]]]
  implicitly[Eq[E4[Int]]]
  implicitly[Eq[E5[Int]]]
  implicitly[Eq[E6[Int]]]
  implicitly[Eq[E7[Int]]]
  implicitly[Eq[E8[Int]]]
  implicitly[Eq[E9[Int]]]
  implicitly[Eq[E10[Int]]]
  implicitly[Eq[E11[Int]]]
  implicitly[Eq[E12[Int]]]
  implicitly[Eq[E13[Int]]]
  implicitly[Eq[E14[Int]]]
  implicitly[Eq[E15[Int]]]
  implicitly[Eq[E16[Int]]]
  implicitly[Pickler[E1[Int]]]
  implicitly[Pickler[E2[Int]]]
  implicitly[Pickler[E3[Int]]]
  implicitly[Pickler[E4[Int]]]
  implicitly[Pickler[E5[Int]]]
  implicitly[Pickler[E6[Int]]]
  implicitly[Pickler[E7[Int]]]
  implicitly[Pickler[E8[Int]]]
  implicitly[Pickler[E9[Int]]]
  implicitly[Pickler[E10[Int]]]
  implicitly[Pickler[E11[Int]]]
  implicitly[Pickler[E12[Int]]]
  implicitly[Pickler[E13[Int]]]
  implicitly[Pickler[E14[Int]]]
  implicitly[Pickler[E15[Int]]]
  implicitly[Pickler[E16[Int]]]
}