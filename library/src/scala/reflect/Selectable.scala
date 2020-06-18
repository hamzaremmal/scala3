package scala.reflect

/** A class that implements structural selections using Java reflection.
 *  It can be used as a supertrait of a class or be made available
 *  as an implicit conversion via `reflectiveSelectable`.
 */
trait Selectable extends scala.Selectable:

  /** The value from which structural members are selected.
   *  By default this is the Selectable instance itself, but it can
   *  be overridden.
   */
  protected def selectedValue: Any = this

  /** Select member with given name */
  def selectDynamic(name: String): Any =
    val rcls = selectedValue.getClass
    try
      val fld = rcls.getField(name)
      ensureAccessible(fld)
      fld.get(selectedValue)
    catch case ex: NoSuchFieldException =>
      applyDynamic(name)()

  /** Select method and apply to arguments.
   *  @param name       The name of the selected method
   *  @param paramTypes The class tags of the selected method's formal parameter types
   *  @param args       The arguments to pass to the selected method
   */
  def applyDynamic(name: String, paramTypes: ClassTag[_]*)(args: Any*): Any =
    val rcls = selectedValue.getClass
    val paramClasses = paramTypes.map(_.runtimeClass)
    val mth = rcls.getMethod(name, paramClasses: _*)
    ensureAccessible(mth)
    mth.invoke(selectedValue, args.asInstanceOf[Seq[AnyRef]]: _*)

object Selectable:

  /** An implicit conversion that turns a value into a Selectable
   *  such that structural selections are performed on that value.
   */
  implicit def reflectiveSelectable(x: Any): Selectable =
    new Selectable { override val selectedValue = x }
