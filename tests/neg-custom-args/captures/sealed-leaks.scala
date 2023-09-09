
import java.io.*
def Test2 =

  def usingLogFile[T](op: (l: caps.Cap) ?-> FileOutputStream^{l} => T): T =
    val logFile = FileOutputStream("log")
    val result = op(logFile)
    logFile.close()
    result

  val later = usingLogFile { f => () => f.write(0) } // error
  val later2 = usingLogFile[(() => Unit) | Null] { f => () => f.write(0) } // error

  var x: (FileOutputStream^) | Null = null
  def foo(f: FileOutputStream^, g: FileOutputStream^) =
    var y = if ??? then f else g  // error

  usingLogFile { f => x = f }  // error

  later()