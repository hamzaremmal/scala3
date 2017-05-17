enum List[+T] {
  case Cons[T](x: T, xs: List[T]) // ok
  case Snoc[U](xs: List[U], x: U) // error: different type parameters
}

enum class X {
  case Y // error: case not allowed here
}

enum E1[T] {
  case C // error: cannot determine type argument
}

enum E2[+T, +U >: T] {
  case C // error: cannot determine type argument
}

enum E3[-T <: Ordered[T]] {
  case C // error: cannot determine type argument
}
