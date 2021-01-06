trait Scope:
   type Expr
   type Value
   def expr(x: String): Expr
   def value(e: Expr): Value
   def combine(e: Expr, str: String): Expr

extension (using s: Scope)(expr: s.Expr)
   def show = expr.toString
   def eval = s.value(expr)
   def *: (str: String) = s.combine(expr, str)

def f(using s: Scope)(x: s.Expr): (String, s.Value) =
   (x.show, x.eval)

given scope: Scope with
   case class Expr(str: String)
   type Value = Int
   def expr(x: String) = Expr(x)
   def value(e: Expr) = e.str.toInt
   def combine(e: Expr, str: String) = Expr(e.str ++ str)

@main def Test =
   val e = scope.Expr("123")
   val (s, v) = f(e)
   println(s)
   println(v)
   val ss = e.show
   println(ss)
   val vv = e.eval
   println(vv)
   val e2 = e *: "4"
   println(e2.show)
   println(e2.eval)

