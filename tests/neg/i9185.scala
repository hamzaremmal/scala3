trait M[F[_]] { def pure[A](x: A): F[A] }
object M {
  def [A, F[A]](x: A).pure(using m: M[F]): F[A] = m.pure(x)
  given listMonad as M[List] { def pure[A](x: A): List[A] = List(x) }
  given optionMonad as M[Option] { def pure[A](x: A): Option[A] = Some(x) }
  val value1: List[String] = "ola".pure
  val value2 = "ola".pure  // error
  val value3 = pure("ola") // error

  def (x: Int).len: Int = x
  val l = "abc".len  // error
}