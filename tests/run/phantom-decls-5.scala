object Test {
  import Boo._

  def main(args: Array[String]): Unit = {
    new Boo5[Blinky](boo[Pinky])
    new Boo5[Pinky](boo[Pinky])
  }

  class Boo5[P <: Blinky](unused p5: P) {
    println("Boo5")
  }
}

object Boo extends Phantom {
  type Blinky <: this.Any
  type Inky <: Blinky
  type Pinky <: Inky
  unused def boo[B <: Blinky]: B = assume
}
