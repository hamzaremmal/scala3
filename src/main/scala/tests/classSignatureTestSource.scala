package tests.classSignatureTestSource

import scala.collection._
import scala.deprecated
import scala.annotation._
import scala.math.{Pi, max}
import example.level2.Documentation

abstract class Documentation[T, A <: Int, B >: String, -X, +Y](c1: String, val c2: List[T]) extends Seq[T] with Product with Serializable
{
    // TODO why we want to compare that?
    // def this(ac: String) = this(ac, Nil)

    def this() = this("", Nil)

    def this(x: T) = this()

    class innerDocumentationClass
    {

    }

    sealed trait CaseImplementThis(id: Int)

    case class IAmACaseClass(x: T, id: Int) extends CaseImplementThis/*<-*/(id)/*->*/

    case object IAmACaseObject extends CaseImplementThis/*<-*/(0)/*->*/

    object testObject
    {

    }

    class Graph
    {
        type Node = Int
    }

    type typeExample[X] >: X <: String

    type abstractType
}

object Documentation
{
  val valInsideDocObject = ???
}

sealed  // TODO support acess and modifiers! // TODO support variance
abstract class ClassExtendingDocumentation[T, A <: Int, B >: String, -X, +Y] extends Documentation[T, A, B, X, Y]
{}

trait TraitTest
{

}

trait TraitWithCompanion{} //expect: trait TraitWithCompanion

object TraitWithCompanion
{}


case class ManyModifiers(/*<-*/val /*->*/x: Int, var y: Double, z: String)
class ManyModifiers2(val x: Int, var y: Double, z: String)