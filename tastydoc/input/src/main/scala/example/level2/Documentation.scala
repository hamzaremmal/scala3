package example
/** Test
*/
package level2

import scala.collection._
import scala.deprecated
import scala.annotation._
import scala.math.{Pi, max}

/** This class is used for testing tasty doc generation
 * @constructor create new object
 * @param c1 class parameter 1
 * @param c2 class parameter 2
 * @tparam T class type parameter
 */
@strictfp
sealed abstract class Documentation[T, Z <: Int](c1: String, val c2: List[T]) extends Seq[T] with Product with Serializable{

  /** Auxiliary constructor
   * @param ac auxiliary parameter
   */
  def this(ac: String) = this(ac, Nil)

  def this() = this("", Nil)

  def this(x: T) = this()

  class innerDocumentationClass {

  }

  object testObject {

  }

  /** Test methods with params
   *
   * @param x parameter 1
   * @param y parameter 2
   *
   * @return something is returned
   */
  def methodsWithParams(x : T, y: Int) : List[Map[Int, T]] = ???

  def methodsWithImplicit(x: Int)(implicit imp: Int, notImp: String) = ???

  /** Test value
  */
  @showAsInfix
  val v : Int = ???

  protected def protectedMethod = ???
  private def privateMethod = ???

  protected val protectedVal = ???
  private val privateVal = ???

  def abstractDefinition : Int

  def apply(idx: Int) = ???
  def iterator = ???
  override def length = ???

  /** An example documention with markdown formatting
   *
   *  **I'm bold**
   *
   *  *I'm italic*
   *
   *  `some code`
   *  ```scala
   *  def someScalaCode(x: String) = println("Hello " + x)
   *  ```
   *  1. I'm a list
   */
  def docWithMd = ???

  def functionWithType[U]() : U

  val complexTypeVal : Int | List[List[T]] & String | (Double | Int, Double) | ((Int) => (String))

  type typeExamle[X] >: X <: String //TypeBound

}

/** Companion object
 */
object Documentation {

}

trait TraitTest {

}