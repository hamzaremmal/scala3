import language.experimental.fewerBraces
class A:
  def f(x: Int)(y: Int): Int = x + y

  f(22).apply(33)

@main def Test =
  val theMap = Map(-1 -> 1, -2 -> 2, 0 -> 3, 1 -> 4, 2 -> 5)
  val res = theMap
    .groupMapReduce: (k, v) =>
      (k + 3) % 3
    .apply: (k, v) =>
      v.toDouble
    .apply: (x, y) =>
      x + y
  println(res)

