package scala
import scala.reflect.ClassTag

/** A marker trait for objects that support structural selection via
 *  `selectDynamic` and `applyDynamic`
 *
 *  Implementation classes should define, or make available as extension
 *  methods, the following two method signatures:
 *
 *    def selectDynamic(name: String): Any
 *    def applyDynamic(name: String)(args: Any*): Any =
 *
 *  `selectDynamic` is invoked for simple selections `v.m`, whereas
 *  `applyDynamic` is invoked for selections with arguments `v.m(...)`.
 *  If there's only one kind of selection, the method supporting the
 *  other may be omitted. The `applyDynamic` can also have a second parameter
 *  list of class tag arguments, i.e. it may alternatively have the signature
 *
 *    def applyDynamic(name: String, paramClasses: ClassTag[_]*)(args: Any*): Any
 *
 *  In this case the call will synthesize `ClassTag` arguments for all formal parameter
 *  types of the method in the structural type.
 */
trait Selectable extends Any
