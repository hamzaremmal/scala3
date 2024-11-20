package dotty.tools.dotc
package transform

import ast.tpd
import core.Contexts.*
import core.Decorators.*
import core.Denotations.*
import core.SymDenotations.*
import core.DenotTransformers.*
import core.Flags.*
import core.Types.*
import config.Printers.inlineTraits
import inlines.InlineTraits

class TraitInlining extends MacroTransform, DenotTransformer:

  import tpd.*

  override def phaseName: String = TraitInlining.name
  override def description: String = TraitInlining.description

  override def changesMembers: Boolean = true

  override protected def newTransformer(using Context): Transformer = new Transformer:
    override def transform(tree: tpd.Tree)(using Context): tpd.Tree =
      tree match
        case tree: Template if ctx.owner.derivesFromInlineTrait =>
          inlineTraits.println(i"Found template for ${ctx.owner} that needs trait inlining")
          val defs = InlineTraits.inlinedDefs(ctx.owner.asClass)
          cpy.Template(tree)(body = defs ::: tree.body)
        case _ =>
          super.transform(tree)
  end newTransformer

  def transform(ref: SingleDenotation)(using Context): SingleDenotation =
    val sym = ref.symbol
    ref match
      case ref: SymDenotation if sym.derivesFromInlineTrait =>
        val tp = ref.info.asInstanceOf[ClassInfo] // TODO: Avoid the `asInstanceOf` call here
        val decls = tp.decls.cloneScope

        // Synthesis the symbols for the inlined members
        for inlinedSymbol <- InlineTraits.synthesiseInlinedMembersSymbols(sym.asClass) do
          println(i"entered inlined memebr symbol $inlinedSymbol to ${sym.asClass} declarations")
          decls.enter(inlinedSymbol)
        
        ref
          .copySymDenotation(info = tp.derivedClassInfo(decls = decls))
          .copyCaches(ref, ctx.phase.next)
      case _ =>
        ref
  end transform


end TraitInlining

object TraitInlining:
  val name: String = "traitInlining"
  val description: String = "synthesise inline trait members in their recepient"