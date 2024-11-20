package dotty.tools.dotc
package transform

import ast.tpd.*
import core.Contexts.*
import core.DenotTransformers.SymTransformer
import core.Flags.*
import core.SymDenotations.*
import core.Symbols.*
import inlines.InlineTraits.*
import MegaPhase.MiniPhase

class DeferInlineTraits extends MiniPhase with SymTransformer:

  override def phaseName: String = DeferInlineTraits.name

  override def description: String = DeferInlineTraits.description

  override def runsAfter: Set[String] = Set(TraitInlining.name, Inlining.name)

  override def transformSym(sym: SymDenotation)(using Context): SymDenotation =
    if sym.symbol.isInlinableMemberInInlineTrait then 
        sym.copySymDenotation(initFlags = sym.flags &~ Final | Deferred)
    else sym

  override def transformDefDef(tree: DefDef)(using Context): DefDef =
    if tree.symbol.isInlinableMemberInInlineTrait then 
        cpy.DefDef(tree)(rhs = EmptyTree)
    else tree

object DeferInlineTraits:
  val name: String = "deferInlineTraits"
  val description: String = "defer all members in inline traits"