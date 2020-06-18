def testLocalInstance =
  trait Zip[F[_]]
  trait Traverse[F[_]] {
    def [A, B, G[_] : Zip](fa: F[A]) traverse(f: A => G[B]): G[F[B]]
  }

  object instances {
    given zipOption as Zip[Option] = ???
    given traverseList as Traverse[List] = ???
  }

  def ff(using xs: Zip[Option]) = ???

  ff // error

  List(1, 2, 3).traverse(x => Option(x)) // error

  locally {
    import instances.traverseList
    List(1, 2, 3).traverse(x => Option(x)) // error
  }
end testLocalInstance