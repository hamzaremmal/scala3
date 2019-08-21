package scala.quoted
package staging

import scala.annotation.implicitNotFound

@implicitNotFound("Could not find implicit scala.quoted.staging.Toolbox.\n\nDefault toolbox can be instantiated with:\n  `implicit val toolbox: scala.quoted.staging.Toolbox = scala.quoted.staging.Toolbox.make(getClass.getClassLoader)`\n\n")
trait Toolbox {
  def run[T](expr: QuoteContext => Expr[T]): T
}

object Toolbox {

  /** Create a new instance of the toolbox using the the classloader of the application.
    *
    * Usuage:
    * ```
    * implicit val toolbox: scala.quoted.staging.Toolbox = scala.quoted.staging.Toolbox.make(getClass.getClassLoader)
    * ```
    *
    * @param appClassloader classloader of the application that generated the quotes
    * @param settings toolbox settings
    * @return A new instance of the toolbox
    */
  def make(appClassloader: ClassLoader)(implicit settings: Settings): Toolbox = {
    try {
      val toolboxImplCls = appClassloader.loadClass("dotty.tools.dotc.quoted.ToolboxImpl")
      val makeMeth = toolboxImplCls.getMethod("make", classOf[Settings], classOf[ClassLoader])
      makeMeth.invoke(null, settings, appClassloader).asInstanceOf[Toolbox]
    }
    catch {
      case ex: ClassNotFoundException =>
        throw new ToolboxNotFoundException(
          s"""Could not load the Toolbox class `${ex.getMessage}` from the JVM classpath. Make sure that the compiler is on the JVM classpath.""",
          ex
        )
    }
  }

  /** Setting of the Toolbox instance. */
  case class Settings private (outDir: Option[String], showRawTree: Boolean, compilerArgs: List[String])

  object Settings {

    implicit def default: Settings = make()

    /** Make toolbox settings
     *  @param outDir Output directory for the compiled quote. If set to None the output will be in memory
     *  @param showRawTree Do not remove quote tree artifacts
     *  @param compilerArgs Compiler arguments. Use only if you know what you are doing.
     */
    def make( // TODO avoid using default parameters (for binary compat)
      showRawTree: Boolean = false,
      outDir: Option[String] = None,
      compilerArgs: List[String] = Nil
    ): Settings =
      new Settings(outDir, showRawTree, compilerArgs)
  }

  class ToolboxNotFoundException(msg: String, cause: ClassNotFoundException) extends Exception(msg, cause)

  class RunScopeException extends Exception("Cannot call `scala.quoted.staging.run(...)` within a macro or another `run(...)`")

}
