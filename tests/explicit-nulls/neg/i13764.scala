import scala.compiletime.uninitialized

var ref: AnyRef   = uninitialized // error
var str: String   = uninitialized // error
var arr: Array[?] = uninitialized // error
