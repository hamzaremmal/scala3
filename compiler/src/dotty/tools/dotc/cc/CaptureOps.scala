package dotty.tools
package dotc
package cc

import core.*
import Types.*, Symbols.*, Contexts.*, Annotations.*, Flags.*
import ast.{tpd, untpd}
import Decorators.*, NameOps.*
import config.SourceVersion
import config.Printers.{capt, ccSetup}
import util.Property.Key
import tpd.*
import StdNames.nme
import config.Feature
import collection.mutable

private val Captures: Key[CaptureSet] = Key()
private val BoxedType: Key[BoxedTypeCache] = Key()

/** Switch whether unpickled function types and byname types should be mapped to
 *  impure types. With the new gradual typing using Fluid capture sets, this should
 *  be no longer needed. Also, it has bad interactions with pickling tests.
 */
private val adaptUnpickledFunctionTypes = false

/** Switch whether we constrain a root var that includes the source of a
 *  root map to be an alias of that source (so that it can be mapped)
 */
private val constrainRootsWhenMapping = true

def allowUniversalInBoxed(using Context) =
  Feature.sourceVersion.isAtLeast(SourceVersion.`3.3`)

/** A dependent function type with given arguments and result type
 *  TODO Move somewhere else where we treat all function type related ops together.
 */
def depFun(args: List[Type], resultType: Type, isContextual: Boolean)(using Context): Type =
  MethodType.companion(isContextual = isContextual)(args, resultType)
    .toFunctionType(alwaysDependent = true)

/** An exception thrown if a @retains argument is not syntactically a CaptureRef */
class IllegalCaptureRef(tpe: Type) extends Exception(tpe.toString)

/** Capture checking state, which is known to other capture checking components */
class CCState:

  /** Cache for level ownership */
  val isLevelOwner: mutable.HashMap[Symbol, Boolean] = new mutable.HashMap

  /** Associates certain symbols (the nesting level owners) with their ccNestingLevel */
  val nestingLevels: mutable.HashMap[Symbol, Int] = new mutable.HashMap

  /** Associates nesting level owners with the local roots valid in their scopes. */
  val localRoots: mutable.HashMap[Symbol, Symbol] = new mutable.HashMap

  /** The last pair of capture reference and capture set where
   *  the reference could not be added to the set due to a level conflict.
   */
  var levelError: Option[(CaptureRef, CaptureSet)] = None

  /** Under saferExceptions: The <try block> symbol generated  for a try.
   *  Installed by Setup, removed by CheckCaptures.
   */
  val tryBlockOwner: mutable.HashMap[Try, Symbol] = new mutable.HashMap

end CCState

/** The currently valid CCState */
def ccState(using Context) =
  Phases.checkCapturesPhase.asInstanceOf[CheckCaptures].ccState

trait FollowAliases extends TypeMap:
  def mapOverFollowingAliases(t: Type): Type = t match
    case t: LazyRef =>
      val t1 = this(t.ref)
      if t1 ne t.ref then t1 else t
    case _ =>
      val t1 = t.dealiasKeepAnnots
      if t1 ne t then
        val t2 = this(t1)
        if t2 ne t1 then return t2
      mapOver(t)

class mapRoots(from: CaptureRoot, to: CaptureRoot)(using Context) extends BiTypeMap, FollowAliases:
  thisMap =>

  def apply(t: Type): Type =
    if t eq from then to
    else t match
      case t: CaptureRoot.Var =>
        val ta = t.followAlias
        if ta ne t then apply(ta)
        else from match
          case from: TermRef
          if t.upperLevel >= from.symbol.ccNestingLevel
            && constrainRootsWhenMapping   // next two lines do the constraining
            && CaptureRoot.isEnclosingRoot(from, t)
            && CaptureRoot.isEnclosingRoot(t, from) => to
          case from: CaptureRoot.Var if from.followAlias eq t => to
          case _ => t
      case t @ Setup.Box(t1) =>
        t.derivedBox(this(t1))
      case _ =>
        mapOverFollowingAliases(t)

  def inverse = mapRoots(to, from)
end mapRoots

extension (tree: Tree)

  /** Map tree with CaptureRef type to its type, throw IllegalCaptureRef otherwise */
  def toCaptureRef(using Context): CaptureRef = tree match
    case QualifiedRoot(outer) =>
      ctx.owner.levelOwnerNamed(outer)
        .orElse(defn.captureRoot) // non-existing outer roots are reported in Setup's checkQualifiedRoots
        .localRoot.termRef
    case _ => tree.tpe match
      case ref: CaptureRef => ref
      case tpe => throw IllegalCaptureRef(tpe) // if this was compiled from cc syntax, problem should have been reported at Typer

  /** Convert a @retains or @retainsByName annotation tree to the capture set it represents.
   *  For efficience, the result is cached as an Attachment on the tree.
   */
  def toCaptureSet(using Context): CaptureSet =
    tree.getAttachment(Captures) match
      case Some(refs) => refs
      case None =>
        val refs = CaptureSet(tree.retainedElems.map(_.toCaptureRef)*)
          .showing(i"toCaptureSet $tree --> $result", capt)
        tree.putAttachment(Captures, refs)
        refs

  /** The arguments of a @retains or @retainsByName annotation */
  def retainedElems(using Context): List[Tree] = tree match
    case Apply(_, Typed(SeqLiteral(elems, _), _) :: Nil) => elems
    case _ => Nil

  /** Under pureFunctions, add a @retainsByName(*)` annotation to the argument of
   *  a by name parameter type, turning the latter into an impure by name parameter type.
   */
  def adaptByNameArgUnderPureFuns(using Context): Tree =
    if adaptUnpickledFunctionTypes && Feature.pureFunsEnabledSomewhere then
      val rbn = defn.RetainsByNameAnnot
      Annotated(tree,
        New(rbn.typeRef).select(rbn.primaryConstructor).appliedTo(
          Typed(
            SeqLiteral(ref(defn.captureRoot) :: Nil, TypeTree(defn.AnyType)),
            TypeTree(defn.RepeatedParamType.appliedTo(defn.AnyType))
          )
        )
      )
    else tree

extension (tp: Type)

  /** @pre `tp` is a CapturingType */
  def derivedCapturingType(parent: Type, refs: CaptureSet)(using Context): Type = tp match
    case tp @ CapturingType(p, r) =>
      if (parent eq p) && (refs eq r) then tp
      else CapturingType(parent, refs, tp.isBoxed)

  // TODO Move boxed/unboxed to CaapturingType?
  /** If this is a unboxed capturing type with nonempty capture set, its boxed version.
   *  Or, if type is a TypeBounds of capturing types, the version where the bounds are boxed.
   *  The identity for all other types.
   */
  def boxed(using Context): Type = tp.dealias match
    case tp @ CapturingType(parent, refs) if !tp.isBoxed && !refs.isAlwaysEmpty =>
      tp.annot match
        case ann: CaptureAnnotation =>
          AnnotatedType(parent, ann.boxedAnnot)
        case ann =>
          ann.tree.getAttachment(BoxedType) match // TODO drop
            case None => ann.tree.putAttachment(BoxedType, BoxedTypeCache())
            case _ =>
          ann.tree.attachment(BoxedType)(tp)
    case tp: RealTypeBounds =>
      tp.derivedTypeBounds(tp.lo.boxed, tp.hi.boxed)
    case _ =>
      tp

  /** If this is a unboxed capturing type with nonempty capture set, its boxed version.
   *  Or, if type is a TypeBounds of capturing types, the version where the bounds are boxed.
   *  The identity for all other types.
   */
  def unboxed(using Context): Type = tp.dealias match
    case tp @ CapturingType(parent, refs) if tp.isBoxed && !refs.isAlwaysEmpty =>
      CapturingType(parent, refs)
    case tp: RealTypeBounds =>
      tp.derivedTypeBounds(tp.lo.unboxed, tp.hi.unboxed)
    case _ =>
      tp

  /** The capture set consisting of all top-level captures of `tp` that appear under a box.
   *  Unlike for `boxed` this also considers parents of capture types, unions and
   *  intersections, and type proxies other than abstract types.
   */
  def boxedCaptureSet(using Context): CaptureSet =
    def getBoxed(tp: Type): CaptureSet = tp match
      case tp @ CapturingType(parent, refs) =>
        val pcs = getBoxed(parent)
        if tp.isBoxed then refs ++ pcs else pcs
      case tp: TypeRef if tp.symbol.isAbstractType => CaptureSet.empty
      case tp: TypeProxy => getBoxed(tp.superType)
      case tp: AndType => getBoxed(tp.tp1) ** getBoxed(tp.tp2)
      case tp: OrType => getBoxed(tp.tp1) ++ getBoxed(tp.tp2)
      case _ => CaptureSet.empty
    getBoxed(tp)

  /** Is the boxedCaptureSet of this type nonempty? */
  def isBoxedCapturing(using Context) = !tp.boxedCaptureSet.isAlwaysEmpty

  /** If this type is a capturing type, the version with boxed statues as given by `boxed`.
   *  If it is a TermRef of a capturing type, and the box status flips, widen to a capturing
   *  type that captures the TermRef.
   */
  def forceBoxStatus(boxed: Boolean)(using Context): Type = tp.widenDealias match
    case tp @ CapturingType(parent, refs) if tp.isBoxed != boxed =>
      val refs1 = tp match
        case ref: CaptureRef if ref.isTracked => ref.singletonCaptureSet
        case _ => refs
      CapturingType(parent, refs1, boxed)
    case _ =>
      tp

  /** Map capturing type to their parents. Capturing types accessible
   *  via dealising are also stripped.
   */
  def stripCapturing(using Context): Type = tp.dealiasKeepAnnots match
    case CapturingType(parent, _) =>
      parent.stripCapturing
    case atd @ AnnotatedType(parent, annot) =>
      atd.derivedAnnotatedType(parent.stripCapturing, annot)
    case _ =>
      tp

  /** Under pureFunctions, map regular function type to impure function type
   */
  def adaptFunctionTypeUnderPureFuns(using Context): Type = tp match
    case AppliedType(fn, args)
    if adaptUnpickledFunctionTypes && Feature.pureFunsEnabledSomewhere && defn.isFunctionClass(fn.typeSymbol) =>
      val fname = fn.typeSymbol.name
      defn.FunctionType(
        fname.functionArity,
        isContextual = fname.isContextFunction,
        isImpure = true).appliedTo(args)
    case _ =>
      tp

  /** Under pureFunctions, add a @retainsByName(*)` annotation to the argument of
   *  a by name parameter type, turning the latter into an impure by name parameter type.
   */
  def adaptByNameArgUnderPureFuns(using Context): Type =
    if adaptUnpickledFunctionTypes && Feature.pureFunsEnabledSomewhere then
      AnnotatedType(tp,
        CaptureAnnotation(CaptureSet.universal, boxed = false)(defn.RetainsByNameAnnot))
    else
      tp

  /** Is type known to be always pure by its class structure,
   *  so that adding a capture set to it would not make sense?
   */
  def isAlwaysPure(using Context): Boolean = tp.dealias match
    case tp: (TypeRef | AppliedType) =>
      val sym = tp.typeSymbol
      if sym.isClass then sym.isPureClass
      else tp.superType.isAlwaysPure
    case CapturingType(parent, refs) =>
      parent.isAlwaysPure || refs.isAlwaysEmpty
    case tp: TypeProxy =>
      tp.superType.isAlwaysPure
    case tp: AndType =>
      tp.tp1.isAlwaysPure || tp.tp2.isAlwaysPure
    case tp: OrType =>
      tp.tp1.isAlwaysPure && tp.tp2.isAlwaysPure
    case _ =>
      false

  def isCapabilityClassRef(using Context) = tp match
    case _: TypeRef | _: AppliedType => tp.typeSymbol.hasAnnotation(defn.CapabilityAnnot)
    case _ => false

  /** Drop @retains annotations everywhere */
  def dropAllRetains(using Context): Type = // TODO we should drop retains from inferred types before unpickling
    val tm = new TypeMap:
      def apply(t: Type) = t match
        case AnnotatedType(parent, annot) if annot.symbol == defn.RetainsAnnot =>
          apply(parent)
        case _ =>
          mapOver(t)
    tm(tp)

extension (cls: Symbol)

  def pureBaseClass(using Context): Option[Symbol] =
    if cls.isClass then cls.asClass.baseClasses.find: bc =>
      defn.pureBaseClasses.contains(bc)
      || bc.givenSelfType.dealiasKeepAnnots.match
          case CapturingType(_, refs) => refs.isAlwaysEmpty
          case RetainingType(_, refs) => refs.isEmpty // TODO: Better: test at phase cc instead?
          case selfType => selfType.exists && selfType.captureSet.isAlwaysEmpty
    else None

extension (sym: Symbol)

  /** A class is pure if:
   *   - one its base types has an explicitly declared self type with an empty capture set
   *   - or it is a value class
   *   - or it is an exception
   *   - or it is one of Nothing, Null, or String
   */
  def isPureClass(using Context): Boolean = sym match
    case cls: ClassSymbol =>
      cls.pureBaseClass.isDefined || defn.pureSimpleClasses.contains(cls)
    case _ =>
      false

  /** Does this symbol allow results carrying the universal capability?
   *  Currently this is true only for function type applies (since their
   *  results are unboxed) and `erasedValue` since this function is magic in
   *  that is allows to conjure global capabilies from nothing (aside: can we find a
   *  more controlled way to achieve this?).
   *  But it could be generalized to other functions that so that they can take capability
   *  classes as arguments.
   */
  def allowsRootCapture(using Context): Boolean =
    sym == defn.Compiletime_erasedValue
    || defn.isFunctionClass(sym.maybeOwner)

  /** When applying `sym`, would the result type be unboxed?
   *  This is the case if the result type contains a top-level reference to an enclosing
   *  class or method type parameter and the method does not allow root capture.
   *  If the type parameter is instantiated to a boxed type, that type would
   *  have to be unboxed in the method's result.
   */
  def unboxesResult(using Context): Boolean =
    def containsEnclTypeParam(tp: Type): Boolean = tp.strippedDealias match
      case tp @ TypeRef(pre: ThisType, _) => tp.symbol.is(Param)
      case tp: TypeParamRef => true
      case tp: AndOrType => containsEnclTypeParam(tp.tp1) || containsEnclTypeParam(tp.tp2)
      case tp: RefinedType => containsEnclTypeParam(tp.parent) || containsEnclTypeParam(tp.refinedInfo)
      case _ => false
    containsEnclTypeParam(sym.info.finalResultType)
    && !sym.allowsRootCapture
    && sym != defn.Caps_unsafeBox
    && sym != defn.Caps_unsafeUnbox

  // Not yet needed
  def takesCappedParam(using Context): Boolean =
    def search = new TypeAccumulator[Boolean]:
      def apply(x: Boolean, t: Type): Boolean = //reporting.trace.force(s"hasCapAt $v, $t"):
        if x then true
        else t match
          case t @ AnnotatedType(t1, annot)
          if annot.symbol == defn.RetainsAnnot || annot.symbol == defn.RetainsByNameAnnot =>
            val elems = annot match
              case CaptureAnnotation(refs, _) => refs.elems.toList
              case _ => annot.tree.retainedElems.map(_.tpe)
            if elems.exists(_.widen.isRef(defn.Caps_Cap)) then true
            else !t1.isCapabilityClassRef && this(x, t1)
          case t: PolyType =>
            apply(x, t.resType)
          case t: MethodType =>
            t.paramInfos.exists(apply(false, _))
          case _ =>
            if t.isRef(defn.Caps_Cap) || t.isCapabilityClassRef then true
            else
              val t1 = t.dealiasKeepAnnots
              if t1 ne t then this(x, t1)
              else foldOver(x, t)
    true || sym.info.stripPoly.match
      case mt: MethodType =>
        mt.paramInfos.exists(search(false, _))
      case _ =>
        false

  // TODO Also include vals (right now they are manually entered in levelOwners by Setup)
  def isLevelOwner(using Context): Boolean =
    val symd = sym.denot
    def isCaseClassSynthetic = // TODO drop
      symd.maybeOwner.isClass && symd.owner.is(Case) && symd.is(Synthetic) && symd.info.firstParamNames.isEmpty
    def compute =
      if symd.isClass then
        symd.is(CaptureChecked) || symd.isRoot
      else
        symd.is(Method, butNot = Accessor)
        && (!symd.owner.isClass
            || symd.owner.is(CaptureChecked)
            || Synthetics.needsTransform(symd)
            )
        //&& !Synthetics.isExcluded(sym)
        && !isCaseClassSynthetic
        && !symd.isConstructor
        && (!symd.isAnonymousFunction || sym.definedLocalRoot.exists)
    ccState.isLevelOwner.getOrElseUpdate(sym, compute)

  /** The owner of the current level. Qualifying owners are
   *   - methods other than constructors and anonymous functions
   *   - anonymous functions, provided they either define a local
   *     root of type caps.Cap, or they are the rhs of a val definition.
   *   - classes, if they are not staticOwners
   *   - _root_
   */
  def levelOwner(using Context): Symbol =
    def recur(sym: Symbol): Symbol =
      if !sym.exists || sym.isRoot || sym.isStaticOwner then defn.RootClass
      else if sym.isLevelOwner then sym
      else recur(sym.owner)
    recur(sym)

  /** The level owner enclosing `sym` which has the given name, or NoSymbol if none exists.
   *  If name refers to a val that has a closure as rhs, we return the closure as level
   *  owner.
   */
  def levelOwnerNamed(name: String)(using Context): Symbol =
    def recur(sym: Symbol, prev: Symbol): Symbol =
      if sym.name.toString == name then
        if sym.isLevelOwner then sym
        else if sym.isTerm && !sym.isOneOf(Method | Module) && prev.exists then prev
        else NoSymbol
      else if sym == defn.RootClass then
        NoSymbol
      else
        val prev1 = if sym.isAnonymousFunction && sym.isLevelOwner then sym else NoSymbol
        recur(sym.owner, prev1)
    recur(sym, NoSymbol)
      .showing(i"find outer $sym [ $name ] = $result", capt)

  /** The nesting level of `sym` for the purposes of `cc`,
   *  -1 for NoSymbol
   */
  def ccNestingLevel(using Context): Int =
    if sym.exists then
      val lowner = sym.levelOwner
      ccState.nestingLevels.getOrElseUpdate(lowner,
        if lowner.isRoot then 0 else lowner.owner.ccNestingLevel + 1)
    else -1

  /** Optionally, the nesting level of `sym` for the purposes of `cc`, provided
   *  a capture checker is running.
   */
  def ccNestingLevelOpt(using Context): Option[Int] =
    if ctx.phase == Phases.checkCapturesPhase || ctx.phase == Phases.checkCapturesPhase.prev
    then Some(ccNestingLevel)
    else None

  /** The parameter with type caps.Cap in the leading term parameter section,
   *  or NoSymbol, if none exists.
   */
  def definedLocalRoot(using Context): Symbol =
    sym.paramSymss.dropWhile(psyms => psyms.nonEmpty && psyms.head.isType) match
      case psyms :: _ => psyms.find(_.info.typeSymbol == defn.Caps_Cap).getOrElse(NoSymbol)
      case _ => NoSymbol

  /** The local root corresponding to sym's level owner */
  def localRoot(using Context): Symbol =
    val owner = sym.levelOwner
    assert(owner.exists)
    def newRoot = newSymbol(if owner.isClass then newLocalDummy(owner) else owner,
      nme.LOCAL_CAPTURE_ROOT, Synthetic, defn.Caps_Cap.typeRef, nestingLevel = owner.ccNestingLevel)
    def lclRoot =
      if owner.isTerm then owner.definedLocalRoot.orElse(newRoot)
      else newRoot
    ccState.localRoots.getOrElseUpdate(owner, lclRoot)

  def maxNested(other: Symbol)(using Context): Symbol =
    if sym.ccNestingLevel < other.ccNestingLevel then other else sym
    /* does not work yet, we do mix sets with different levels, for instance in cc-this.scala.
    else if sym.ccNestingLevel > other.ccNestingLevel then sym
    else
      assert(sym == other, i"conflicting symbols at same nesting level: $sym, $other")
      sym
    */

  def minNested(other: Symbol)(using Context): Symbol =
    if sym.ccNestingLevel > other.ccNestingLevel then other else sym

extension (tp: TermRef | ThisType)
  /** The nesting level of this reference as defined by capture checking */
  def ccNestingLevel(using Context): Int = tp match
    case tp: TermRef => tp.symbol.ccNestingLevel
    case tp: ThisType => tp.cls.ccNestingLevel

extension (tp: AnnotatedType)
  /** Is this a boxed capturing type? */
  def isBoxed(using Context): Boolean = tp.annot match
    case ann: CaptureAnnotation => ann.boxed
    case _ => false
