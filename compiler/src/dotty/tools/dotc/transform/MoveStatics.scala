package dotty.tools.dotc
package transform

import core.*
import Flags.*
import Contexts.*
import Symbols.*
import DenotTransformers.SymTransformer
import Types.MethodType
import Annotations.Annotation
import SymDenotations.SymDenotation
import Names.Name
import StdNames.nme
import NameOps.*

import ast.*


import MegaPhase.*

/** Move static methods from companion to the class itself */
class MoveStatics extends MiniPhase with SymTransformer {
  import ast.tpd.*

  override def phaseName: String = MoveStatics.name

  override def description: String = MoveStatics.description

  def transformSym(sym: SymDenotation)(using Context): SymDenotation =
    if (sym.hasAnnotation(defn.ScalaStaticAnnot) && sym.owner.is(Flags.Module) && sym.owner.companionClass.exists &&
        (sym.is(Flags.Method) || !(sym.is(Flags.Mutable) && sym.owner.companionClass.is(Flags.Trait)))) {
      sym.owner.asClass.delete(sym.symbol)
      sym.owner.companionClass.asClass.enter(sym.symbol)
      sym.copySymDenotation(owner = sym.owner.companionClass)
    }
    else sym

  /*override def transformStats(trees: List[Tree])(using Context): List[Tree] =
    if (ctx.owner.is(Flags.Package)) {
      val (classes, others) = trees.partition(x => x.isInstanceOf[TypeDef] && x.symbol.isClass)
      val pairs = classes.groupBy(_.symbol.name.stripModuleClassSuffix).asInstanceOf[Map[Name, List[TypeDef]]]

      def rebuild(orig: TypeDef, newBody: List[Tree]): Tree = {
        val staticFields = newBody.filter(x => x.isInstanceOf[ValDef] && x.symbol.hasAnnotation(defn.ScalaStaticAnnot)).asInstanceOf[List[ValDef]]
        val newBodyWithStaticConstr =
          if (staticFields.nonEmpty) {
            /* do NOT put Flags.JavaStatic here. It breaks .enclosingClass */
            val staticCostructor = newSymbol(orig.symbol, nme.STATIC_CONSTRUCTOR, Flags.Synthetic | Flags.Method | Flags.Private, MethodType(Nil, defn.UnitType))
            staticCostructor.addAnnotation(Annotation(defn.ScalaStaticAnnot, staticCostructor.span))
            staticCostructor.entered

            val staticAssigns = staticFields.map(x => Assign(ref(x.symbol), x.rhs.changeOwner(x.symbol, staticCostructor)))
            tpd.DefDef(staticCostructor, Block(staticAssigns, tpd.unitLiteral)) :: newBody
          }
          else newBody

        val oldTemplate = orig.rhs.asInstanceOf[Template]
        cpy.TypeDef(orig)(rhs = cpy.Template(oldTemplate)(body = newBodyWithStaticConstr))
      }

      def move(module: TypeDef, companion: TypeDef): List[Tree] = {
        assert(companion != module)
        if (!module.symbol.is(Flags.Module)) move(companion, module)
        else {
          val moduleTmpl = module.rhs.asInstanceOf[Template]
          val companionTmpl = companion.rhs.asInstanceOf[Template]
          val (staticDefs, remainingDefs) = moduleTmpl.body.partition {
            case memberDef: MemberDef => memberDef.symbol.isScalaStatic
            case _ => false
          }

          rebuild(companion, companionTmpl.body ++ staticDefs) :: rebuild(module, remainingDefs) :: Nil
        }
      }
      val newPairs =
        for ((name, classes) <- pairs)
          yield
            if (classes.tail.isEmpty) {
              val classDef = classes.head
              val tmpl = classDef.rhs.asInstanceOf[Template]
              rebuild(classDef, tmpl.body) :: Nil
            }
            else move(classes.head, classes.tail.head)
      Trees.flatten(newPairs.toList.flatten ++ others)
    }
    else trees*/

    /**@note This implementation assumes that a module and its companion are consecutive to each other in trees (See [[Checker.checkStatsOrder]]) */
  override def transformStats(trees: List[Tree])(using Context): List[Tree] =
    def tryMovingStatics(trees: List[Tree]): List[Tree] =
      def rebuild(orig: TypeDef, newBody: List[Tree]): Tree =
        val staticFields = newBody.filter(x => x.isInstanceOf[ValDef] && x.symbol.hasAnnotation(defn.ScalaStaticAnnot)).asInstanceOf[List[ValDef]]
        val newBodyWithStaticConstr =
          if staticFields.nonEmpty then
            /* do NOT put Flags.JavaStatic here. It breaks .enclosingClass */
            val staticCostructor = newSymbol(orig.symbol, nme.STATIC_CONSTRUCTOR, Flags.Synthetic | Flags.Method | Flags.Private, MethodType(Nil, defn.UnitType))
            staticCostructor.addAnnotation(Annotation(defn.ScalaStaticAnnot, staticCostructor.span))
            staticCostructor.entered
            val staticAssigns = staticFields.map(x => Assign(ref(x.symbol), x.rhs.changeOwner(x.symbol, staticCostructor)))
            tpd.DefDef(staticCostructor, Block(staticAssigns, tpd.unitLiteral)) :: newBody
          else newBody
        val oldTemplate = orig.rhs.asInstanceOf[Template]
        cpy.TypeDef(orig)(rhs = cpy.Template(oldTemplate)(body = newBodyWithStaticConstr))
      end rebuild

      def move(module: TypeDef, companion: TypeDef): (Tree, Tree) =
        assert(companion != module)
        if !module.symbol.is(Flags.Module) then move(companion, module)
        else
          val moduleTmpl = module.rhs.asInstanceOf[Template]
          val companionTmpl = companion.rhs.asInstanceOf[Template]
          val (staticDefs, remainingDefs) = moduleTmpl.body.partition:
            case memberDef: MemberDef => memberDef.symbol.isScalaStatic
            case _ => false
          (rebuild(companion, companionTmpl.body ++ staticDefs), rebuild(module, remainingDefs))
      end move

      trees match
        case (cls: TypeDef) :: (mod: ValDef) :: (modcls: TypeDef) :: xs if cls.symbol.companionModule.exists && mod.symbol.is(ModuleVal) =>
          // We have the guarantee that cls, mod and modcls are related (See documentation above)
          val tree = move(cls, modcls)
          tree._1 :: mod :: tree._2  :: tryMovingStatics(xs)
        case (mod: ValDef) :: (cls: TypeDef) :: xs if mod.mods.is(ModuleVal) && mod.mods.is(ModuleClass) =>
          // We have the guarantee that mod and cls are related (See documentation above)
          val tmpl = cls.rhs.asInstanceOf[Template]
          mod :: rebuild(cls, tmpl.body) :: tryMovingStatics(xs)
        case (cls: TypeDef) :: xs if cls.symbol.isClass =>
          rebuild(cls, cls.rhs.asInstanceOf[Template].body) :: tryMovingStatics(xs)
        case x :: xs => // Nothing to transform, skip it
          x :: tryMovingStatics(xs)
        case Nil => Nil
    end tryMovingStatics

    if (ctx.owner.is(Flags.Package)) then tryMovingStatics(trees)
    else trees
  end transformStats
}

object MoveStatics {
  val name: String = "moveStatic"
  val description: String = "move static methods from companion to the class itself"
}
