import language.experimental.captureChecking
import annotation.experimental
import caps.Sharable
import caps.use

@experimental object Test2:

  class List[+A]:
    def map[B](f: A => B): List[B] = ???

  class Label extends Sharable

  class Listener

  def test2(@use lbls: List[Label]) =
    def makeListener(lbl: Label): Listener^{lbl} = ???
    val listeners = lbls.map(makeListener) // should work

