package foo

trait Foo { def g(x: Any): Any }

inline given f[T <: Foo] as T = ??? match {
  case x: T => x.g(10) // error
}

@main def Test = f // error // error
