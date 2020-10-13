object lst:
  opaque type Lst[+T] = Any
  object Lst:
    given lstEql[T, U] as Eql[Lst[T], Lst[U]] = Eql.derived
    val Empty: Lst[Nothing] = ???
end lst

import lst.Lst

val xs: Lst[Int] = ???
val ys: List[Int] = ???

def test =
  xs == ys                  // error: cannot be compared
  ys == xs                  // error
  xs == Nil                 // error
  Nil == xs                 // error
  ys == Lst.Empty           // error
  Lst.Empty == ys           // error
  xs match
    case Nil => ???         // error, used to pass
  ys match
    case Lst.Empty => ???   // error, used to pass


