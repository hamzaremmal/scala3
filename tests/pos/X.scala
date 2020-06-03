import scala.deriving._

trait FunctorK[F[_[_]]]
object FunctorK {
  given [C] as FunctorK[[F[_]] =>> C] {}
  given [T] as FunctorK[[F[_]] =>> Tuple1[F[T]]]

  def derived[F[_[_]]](using m: Mirror { type MirroredType[X[_]] = F[X] ; type MirroredElemTypes[_[_]] }, r: FunctorK[m.MirroredElemTypes]): FunctorK[F] = new FunctorK[F] {}
}

