package dotty.tools.dotc
package transform

import core.*
import Contexts.*
import Flags.*
import Symbols.*
import Decorators.*
import MegaPhase.MiniPhase
import StdNames.nme
import ast.tpd
import dotty.tools.dotc.core.NullOpsDecorator.isNullableUnion

/** This phase replaces `compiletime.uninitialized` on the right hand side of a mutable field definition by `_`.
 *  This avoids a
 *  ```scala
 *  "@compileTimeOnly("`uninitialized` can only be used as the right hand side of a mutable field definition")`
 *  ```
 *  error in Erasure and communicates to Constructors that the variable does not have an initializer.
 *
 *  @syntax markdown
 */
class UninitializedDefs extends MiniPhase:
  import tpd.*

  override def phaseName: String = UninitializedDefs.name

  override def description: String = UninitializedDefs.description

  override def transformValDef(tree: ValDef)(using Context): Tree =
    if !hasUninitializedRHS(tree) then tree
    else cpy.ValDef(tree)(rhs = cpy.Ident(tree.rhs)(nme.WILDCARD).withType(tree.tpt.tpe))

  private def hasUninitializedRHS(tree: ValDef)(using Context): Boolean =
    def recur(rhs: Tree): Boolean = rhs match
      case rhs: RefTree if rhs.symbol == defn.Compiletime_uninitialized =>
        val tpe = tree.tpt.tpe
        // Do not allow compiletime.uninitialized when -Yexplicit-nulls is enabled unless explicitly added in the type
        if ctx.explicitNulls && !tpe.isPrimitiveValueType && !TypeComparer.isSubType(defn.NullType, tpe) then
          report.error(em"""The usage of ${defn.Compiletime_uninitialized} is not allow here.
                            |
                            |To fix the issue, either:
                            |1- Disable explicit nulls
                            |2- Add explicitly the ${defn.NullType} type to the left hand side of the definition:
                            |   ${cpy.ValDef(tree)(tpt = TypeTree(Types.OrNull(tpe)))}
                            |""", tree.srcPos)
        tree.symbol.is(Mutable) && tree.symbol.owner.isClass
      case closureDef(ddef) if defn.isContextFunctionType(tree.tpt.tpe.dealias) =>
        recur(ddef.rhs)
      case _ =>
        false
    recur(tree.rhs)

end UninitializedDefs

object UninitializedDefs:
  val name: String = "uninitialized"
  val description: String = "eliminates `compiletime.uninitialized`"
end UninitializedDefs
