
object Test {
  import Boo._

  def main(args: Array[String]): Unit = {
    hkFun1(boo[Blinky])
    hkFun1(boo[Inky])
    hkFun1(boo[Pinky])
  }

  type HKPhantom[X <: Blinky] = X

  def hkFun1[Y <: Blinky](unused p9: HKPhantom[Y]) = {
    println("hkFun1")
  }

}

trait Phantoms {
}

object Boo extends Phantom {
  type Blinky <: this.Any
  type Inky <: Blinky
  type Pinky <: Inky
  unused def boo[B <: this.Any]: B = assume
}
