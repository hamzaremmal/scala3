package dotty.tools.dotc
package inlines

import ast.tpd.*
import core.Contexts.*
import core.Decorators.*
import core.Flags.*
import core.Names.*
import core.NameOps.*
import core.Symbols.*
import core.Types.*
import config.Printers.inlineTraits

object InlineTraits:

  extension(sym: Symbol)
      /** Is the symbol an inlinable member from an inline trait */
      def isInlinableMemberInInlineTrait(using Context): Boolean =
        sym.isTerm && sym.owner.isInlineTrait

      /** Is the symbol a synthesised member from an inline trait member */
      def isSynthesisedFromInlineTrait(using Context): Boolean =
        sym.isAllOf(Synthetic | Override) 
        && sym.nextOverriddenSymbol.isInlinableMemberInInlineTrait
      end isSynthesisedFromInlineTrait
  end extension

  // ==============================================================================================
  // ==================================== SYMBOL MANIPULATION =====================================
  // ==============================================================================================

  /** Synthesise the symbols for **inlined-members** */
  def synthesiseInlinedMembersSymbols(cls: ClassSymbol)(using Context): Seq[Symbol] =
    assert(!cls.isInlineTrait, i"Inline traits should not synthesis symbols for inlined members")
    inlineTraits.println(i"Synthesising symbols for inlined trait members in $cls")
    for denot <- cls.typeRef.allMembers
        sym = denot.symbol
        if sym.isInlinableMemberInInlineTrait
    yield synthesiseInlinedMemberSymbol(cls, sym)
  end synthesiseInlinedMembersSymbols

  /** Synthesise the symbol for the *inlined member* `sym` in the class `cls` */
  def synthesiseInlinedMemberSymbol(cls: ClassSymbol, sym: Symbol)(using Context): Symbol =
    assert(sym.isInlinableMemberInInlineTrait, i"Cannot synthesis symbol for non-inlinable $sym in $cls")
    inlineTraits.println(i"Synthesising symbol for inlinable $sym in $cls")
    val flags = sym.flags | Override | Synthetic
    val info = sym.info
      .substThis(sym.owner.asClass, ThisType.raw(cls.typeRef))
    newSymbol(cls, sym.name, flags, info, sym.privateWithin, cls.span)
  end synthesiseInlinedMemberSymbol

  // ==============================================================================================
  // ===================================== TREE MANIPULATION ======================================
  // ==============================================================================================

  def inlinedDefs(cls: ClassSymbol)(using Context): List[Tree] =
    val decls = atPhase(ctx.phase.next) { cls.info.decls.toList }
    decls
      .filter(isSynthesisedFromInlineTrait) // Only keep the generated symbols
      .map(inlinedDefDefForDecl(cls))
  end inlinedDefs

  
  /** Generate the tree for  */
  private def inlinedDefDefForDecl(cls: ClassSymbol)(decl: Symbol)(using Context): Tree =
    assert(decl.isSynthesisedFromInlineTrait, i"cannot synthesise tree for $decl. $decl was not generated from an inline trait member definition")
    inlineTraits.println(i"generating a tree for $decl in $cls")
    val originalSym = decl.nextOverriddenSymbol
    assert(originalSym.isInlinableMemberInInlineTrait, i"Generating tree for $decl while it doesn't override an inline trait member")

    def rhs(argss: List[List[Tree]])(using Context) =
      if decl.is(Deferred) then EmptyTree
      else
        ctx.compilationUnit.needsInlining = true // TODO: This mutation here is akward, move it to PostTyper
        Super(This(cls), originalSym.owner.asType.name).select(originalSym).appliedToArgss(argss)

    if decl.is(Method) then
      DefDef(decl.asTerm, rhs).withSpan(cls.span)
    else
      assert(false, i"$decl is not supported yet")
  end inlinedDefDefForDecl

end InlineTraits