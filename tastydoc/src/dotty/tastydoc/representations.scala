package dotty.tastydoc

import scala.tasty.Reflection
import scala.annotation.tailrec
import dotty.tastydoc.comment.{Comment, WikiComment, MarkdownComment}
import dotty.tastydoc.references._
import dotty.tastydoc.TastyExtractor

object representations extends TastyExtractor {

  def removeColorFromType(tpe: String) : String = { //TODO: This a workaround, fix this
    tpe.replaceAll("\u001B\\[[;\\d]*m", "")
  }

  trait Representation {
    val name : String
    val path : List[String]
    //def comments(packages: Map[String, EmulatedPackageRepresentation]): Option[Comment]
    def comments(packages: Map[String, EmulatedPackageRepresentation]): Option[Comment]
    val parentRepresentation: Option[Representation] //Called simply "parent" in dotty-doc
    val annotations: List[TypeReference]
  }

  trait Parents {
    val parents : List[Reference] //Inheritance similar to supertypes in dotty-doc
  }

  trait Members {
    def members : List[Representation] //Is a def because so we can override with either a var or a val (Needed for EmulatedPackage) //TOASK Efficiency + is ok
  }

  trait Modifiers {
    val modifiers: List[String]
    val privateWithin: Option[Reference]
    val protectedWithin: Option[Reference]

    def isPrivate: Boolean = modifiers.contains("private")
    def isProtected: Boolean = modifiers.contains("protected")
    def isAbstract: Boolean = modifiers.contains("abstract")
  }

  trait Companion {
    val companion: Option[CompanionReference]

    def hasCompanion: Boolean = companion.isDefined //To be consistent with dotty-doc
    // val companionPath_=(xs: List[String]): Unit
  }

  trait ParamList {
    val list: List[NamedReference]
    val isImplicit: Boolean
  }

  trait MultipleParamList {
    val paramLists: List[ParamList]
  }

  trait Constructors {
    val constructors: List[DefRepresentation]
  }

  trait ReturnValue {
    val returnValue: Reference
  }

  trait TypeParams {
    val typeParams: List[String]
  }

  class EmulatedPackageRepresentation(val name: String, val path: List[String]) extends Representation with Members { //This contains all the packages representing a single package
    override val parentRepresentation = None
    override val annotations = Nil
    override def comments(packages: Map[String, EmulatedPackageRepresentation]) = None
    var packagesMembers: List[PackageRepresentation] = Nil

    //From the outisde, calling members is seemless and appears like calling members on a PackageRepresentation
    override def members = {
      @tailrec
      def noDuplicates(seenPackages: Set[String], members: List[Representation], acc: List[Representation]): (List[Representation], Set[String]) = members match {
        case Nil => (acc, seenPackages)
        case (x: PackageRepresentation)::xs if seenPackages.contains(x.name) => noDuplicates(seenPackages, xs, acc)
        case (x: PackageRepresentation)::xs => noDuplicates(seenPackages + x.name, xs, TastydocConsumer.mutablePackagesMap((x.path :+ x.name).mkString(".")) :: acc)
        case x::xs => noDuplicates(seenPackages, xs, x::acc)
      }

      packagesMembers.foldLeft((List.empty[Representation], Set.empty[String]))((acc, p) => noDuplicates(acc._2, p.members, acc._1))._1
    }
  }

  class PackageRepresentation(reflect: Reflection, internal: reflect.PackageClause, override val parentRepresentation: Option[Representation]) extends Representation with Members {
    import reflect._

    override val (name, path) = extractPackageNameAndPath(internal.pid.show)
    override val members = internal.stats.map(convertToRepresentation(reflect)(_, Some(this)))
    override val annotations = extractAnnotations(reflect)(internal.symbol.annots)

    override def comments(packages: Map[String, EmulatedPackageRepresentation]) = extractComments(reflect)(internal.symbol.comment, this)(packages)
  }

  //TODO: Handle impliedOnly
  class ImportRepresentation(reflect: Reflection, internal: reflect.Import, override val parentRepresentation: Option[Representation]) extends Representation {
    import reflect._

    override val name = if (internal.selectors.size > 1){
        internal.selectors.map(_.toString).mkString("{", ", ", "}")
      } else {
        internal.selectors.head.toString
      }
    override val path = internal.expr.symbol.show.split("\\.").toList
    override val annotations = extractAnnotations(reflect)(internal.symbol.annots)

    override def comments(packages: Map[String, EmulatedPackageRepresentation]) = extractComments(reflect)(internal.symbol.comment, this)(packages)
  }

  class ClassRepresentation(reflect: Reflection, internal: reflect.ClassDef, override val parentRepresentation: Option[Representation]) extends Representation with Members with Parents with Modifiers with Companion with Constructors with TypeParams {
    import reflect._

    override val path = extractPath(reflect)(internal.symbol)
    override val parents = extractParents(reflect)(internal.parents)
    override val (modifiers, privateWithin, protectedWithin) = extractModifiers(reflect)(internal.symbol.flags, internal.symbol.privateWithin, internal.symbol.protectedWithin)
    override val constructors =
      (convertToRepresentation(reflect)(internal.constructor, Some(this)) ::
      (internal.body.flatMap{_ match {
        case reflect.IsDefDef(d@reflect.DefDef(_)) => if(d.name == "<init>") Some(d) else None
        case _ => None
        }
      }.map(convertToRepresentation(reflect)(_, Some(this)))
      )).flatMap{r => r match {
        case r: DefRepresentation => Some(r)
        case _ => None
        }
      }
    override val typeParams = internal.constructor.typeParams.map(x => removeColorFromType(x.show).stripPrefix("type "))
    override val annotations = extractAnnotations(reflect)(internal.symbol.annots)
    var knownSubclasses: List[Reference] = Nil

    val (isCase, isTrait, isObject, kind) = extractKind(reflect)(internal.symbol.flags)

    override val name = if(isObject) internal.name.stripSuffix("$") else internal.name

    override val companion = extractCompanion(reflect)(internal.symbol.companionModule, internal.symbol.companionClass, !isObject)
    override val members = extractClassMembers(reflect)(internal.body, internal.symbol, Some(this))

    //Add itself to parents subclasses:
    parentRepresentation match {
      case Some(r: ClassRepresentation) =>
        r.knownSubclasses = CompanionReference(internal.name, path.mkString("/", "/", ""), kind) :: r.knownSubclasses //Hacky solution using CompanionReference so that it is printed the way we want
      case _ =>
    }

    override def comments(packages: Map[String, EmulatedPackageRepresentation]) = extractComments(reflect)(internal.symbol.comment, this)(packages)
  }

  class DefRepresentation(reflect: Reflection, internal: reflect.DefDef, override val parentRepresentation: Option[Representation]) extends Representation with Modifiers with TypeParams with MultipleParamList with ReturnValue {
    import reflect._

    // println(internal.name + "==========") //TOASK Bug again?
    // println()
    // private def test(sym: reflect.Symbol): Unit = sym match {
    //   case reflect.IsClassDefSymbol(classSym) =>
    //     print(classSym.name)
    //     classSym.method(internal.name).filter(_.hashCode == internal.symbol.hashCode) match {
    //       case Nil =>
    //       case x::_ =>
    //           print("-->")
    //           test(x)
    //     }
    //   case _ =>
    // }
    // test(internal.symbol.owner)
    // println()

    override val name = internal.name
    override val path = extractPath(reflect)(internal.symbol)
    override val (modifiers, privateWithin, protectedWithin) = extractModifiers(reflect)(internal.symbol.flags, internal.symbol.privateWithin, internal.symbol.protectedWithin)
    override val typeParams = internal.typeParams.map(x => removeColorFromType(x.show).stripPrefix("type "))

    override val paramLists = internal.paramss.map{p =>
      new ParamList {
        override val list = p.map(x => NamedReference(x.name, convertTypeToReference(reflect)(x.tpt.tpe)))
        override val isImplicit = if(p.size > 1) p.tail.head.symbol.flags.is(Flags.Implicit) else false //TODO: Verfiy this
      }
    }
    override val returnValue = convertTypeToReference(reflect)(internal.returnTpt.tpe)
    override val annotations = extractAnnotations(reflect)(internal.symbol.annots)
    override def comments(packages: Map[String, EmulatedPackageRepresentation]) = extractComments(reflect)(internal.symbol.comment, this)(packages)
  }

  class ValRepresentation(reflect: Reflection, internal: reflect.ValDef, override val parentRepresentation: Option[Representation]) extends Representation with Modifiers with ReturnValue {
    import reflect._

    override val name = internal.name
    override val path = extractPath(reflect)(internal.symbol)
    override val (modifiers, privateWithin, protectedWithin) = extractModifiers(reflect)(internal.symbol.flags, internal.symbol.privateWithin, internal.symbol.protectedWithin)
    override val returnValue = convertTypeToReference(reflect)(internal.tpt.tpe)
    override val annotations = extractAnnotations(reflect)(internal.symbol.annots)
    override def comments(packages: Map[String, EmulatedPackageRepresentation]) = extractComments(reflect)(internal.symbol.comment, this)(packages)
  }

  class TypeRepresentation(reflect: Reflection, internal: reflect.TypeDef, override val parentRepresentation: Option[Representation]) extends Representation with Modifiers with TypeParams {
    import reflect._

    override val name = internal.name
    override val path = extractPath(reflect)(internal.symbol)
    override val (modifiers, privateWithin, protectedWithin) = extractModifiers(reflect)(internal.symbol.flags, internal.symbol.privateWithin, internal.symbol.protectedWithin)
    override val typeParams = Nil
    override val annotations = extractAnnotations(reflect)(internal.symbol.annots)
    val alias: Option[Reference] = internal.rhs match{
      case reflect.IsTypeBoundsTree(t) => Some(convertTypeOrBoundsToReference(reflect)(t.tpe))
      case reflect.IsTypeTree(t) => Some(convertTypeOrBoundsToReference(reflect)(t.tpe.asInstanceOf[reflect.TypeOrBounds]))
      case _ => None
    }
    override def isAbstract: Boolean = !alias.isDefined
    override def comments(packages: Map[String, EmulatedPackageRepresentation]) = extractComments(reflect)(internal.symbol.comment, this)(packages)
  }

  def convertToRepresentation(reflect: Reflection)(tree: reflect.Tree, parentRepresentation: Option[Representation]): Representation = {
    import reflect._

    tree match {
      case IsPackageClause(t@reflect.PackageClause(_)) =>
        val noColorPid = removeColorFromType(t.pid.symbol.show)
        val emulatedPackage = TastydocConsumer.mutablePackagesMap.get(noColorPid) match {
          case Some(x) => x
          case None =>
            val (name, path) = extractPackageNameAndPath(t.pid.symbol.show)
            val x = new EmulatedPackageRepresentation(name, path)
            TastydocConsumer.mutablePackagesMap += ((noColorPid, x))
            x
        }
        val r = new PackageRepresentation(reflect, t, parentRepresentation)
        emulatedPackage.packagesMembers = r :: emulatedPackage.packagesMembers
        r

      case IsImport(t@reflect.Import(_)) => new ImportRepresentation(reflect, t, parentRepresentation)

      case IsClassDef(t@reflect.ClassDef(_)) => new ClassRepresentation(reflect, t, parentRepresentation)

      case IsDefDef(t@reflect.DefDef(_)) => new DefRepresentation(reflect, t, parentRepresentation)

      case IsValDef(t@reflect.ValDef(_)) => new ValRepresentation(reflect, t, parentRepresentation)

      case IsTypeDef(t@reflect.TypeDef(_)) => new TypeRepresentation(reflect, t, parentRepresentation)

      case _ => throw new Exception("Tree match error in conversion to representation. Please open an issue." + tree)
  }}
}