---
layout: blog-page
title: Announcing Dotty 0.15.0-RC1 with TODO
author: Anatolii Kmetiuk
authorImg: /images/anatolii.png
date: 2019-05-24
---

Hi! In this article, we'd like to announce the 15th release of Dotty. With this release comes a bunch of new features and improvements, such as the ability to enforce whether an operator is intended to be used in an infix position, the type safe pattern bindings and more.

This release serves as a technology preview that demonstrates new
language features and the compiler supporting them.

Dotty is the project name for technologies that are being considered for
inclusion in Scala 3. Scala has pioneered the fusion of object-oriented and
functional programming in a typed setting. Scala 3 will be a big step towards
realising the full potential of these ideas. Its main objectives are to

- become more opinionated by promoting programming idioms we found to work well,
- simplify where possible,
- eliminate inconsistencies and surprising behaviours,
- build on strong foundations to ensure the design hangs together well,
- consolidate language constructs to improve the language’s consistency, safety, ergonomics, and
  performance.

You can learn more about Dotty on our [website](https://dotty.epfl.ch).

<!--more-->

This is our 15th scheduled release according to our
[6-week release schedule](https://dotty.epfl.ch/docs/contributing/release.html).

# What’s new in the 0.15.0-RC1 technology preview?
## Operator Rules
This change addresses the problem of the regulation of whether an operator is supposed to be used in an infix position. The motivation is for the library authors to be able to enforce whether a method or a type is supposed to be used in an infix position by the users. This ability will help to make code bases more consistent in the way the calls to methods are performed.

Methods with symbolic names like `+` are allowed to be used in an infix position by default:

```scala
scala> case class Foo(x: Int) { def +(other: Foo) = x + other.x }
// defined case class Foo

scala> Foo(1) + Foo(2)
val res0: Int = 3
```

Methods with alphanumeric names are not allowed to be used in an infix position. Breaking this constraint will raise a deprecation warning:

```scala
scala> case class Foo(x: Int) { def plus(other: Foo) = x + other.x }
// defined case class Foo

scala> Foo(1) plus Foo(2)
1 |Foo(1) plus Foo(2)
  |       ^^^^
  |Alphanumeric method plus is not declared @infix; it should not be used as infix operator.
  |The operation can be rewritten automatically to `plus` under -deprecation -rewrite.
  |Or rewrite to method syntax .plus(...) manually.
val res1: Int = 3

scala> Foo(1).plus(Foo(2))
val res2: Int = 3
```

As the warning says, if you want the users of the method to be able to use it in an infix position, you can do so as follows:

```scala
scala> import scala.annotation.infix

scala> case class Foo(x: Int) { @infix def plus(other: Foo) = x + other.x }
// defined case class Foo

scala> Foo(1) plus Foo(2)
val res3: Int = 3
```

To smoothen the migration, the deprecation warnings will only be emitted if you compile with the `-strict` flag under Dotty 3. Alphanumeric methods that are defined without the `@infix` annotation used in an infix position will be deprecated by default starting with Dotty 3.1.

For more information, see the the [documentation](http://dotty.epfl.ch/docs/reference/changed-features/operators.html#the-infix-annotation). Note that the `@alpha` annotation also described in the documentation is planned for the future and is not available in this release.

## `given` clause comes last
In the previous release, you could write something like this:

```scala
implied for String = "foo"
def f(x: Int) given (y: String) (z: Int) = x + z
f(1)(3)
```

Now, however, `given` clauses must come last. The above code will fail with:

```
-- Error: ../issues/Playground.scala:3:34 --------------------------------------
3 |  def f(x: Int) given (y: String) (z: Int) = x + z
  |                                  ^
  |                       normal parameters cannot come after `given' clauses
one error found
```

The following snippet is the correct way to express the program in question:

```scala
implied for String = "foo"
def f(x: Int)(z: Int) given (y: String) = x + z
f(1)(3)
```

We changed this to reduce confusion when calling functions with mixed explicit and implied parameters.

## Type-safe Pattern Bindings
```scala
  val xs: List[Any] = List(1, 2, 3)
  val (x: String) :: _ = xs   // error: pattern's type String is more specialized
                              // than the right hand side expression's type Any
```

The above code will fail with a compile-time error in Dotty 3.1 and in Dotty 3 with the `-strict` flag. In contrast, in Scala 2, the above would have compiled fine but failed on runtime with an exception.

Dotty compiler will allow such a pattern binding only if the pattern is *irrefutable* – that is, if the right-hand side conforms to the pattern's type. E.g. the following is OK:

```scala
  val pair = (1, true)
  val (x, y) = pair
```

If we want to force the pattern binding if the pattern is not irrefutable, we can do so with an annotation:

```scala
  val xs: List[Any] = List("1", "2", "3")
  val (x: String) :: _: @unchecked = xs
```

The same is implemented for pattern bindings in `for` expressions:

```scala
  val elems: List[Any] = List((1, 2), "hello", (3, 4))
  for ((x, y) <- elems) yield (y, x) // error: pattern's type (Any, Any) is more specialized
                                     // than the right hand side expression's type Any
```

For the migration purposes, the above change will only take effect in Dotty 3.1. You can use it in Dotty 3 with the `-strict` flag.

For more information, see the [documentation](http://dotty.epfl.ch/docs/reference/changed-features/pattern-bindings.html).

## Further improvements to Generalised Algebraic Data Types (GADTs) support
In this release, we've further improved our support for GADTs. Most notably, we now support variant GADTs:

```scala
enum Expr[+T] {
  case StrLit(s: String) extends Expr[String]
  case Pair[A, B](a: Expr[A], b: Expr[B]) extends Expr[(A, B)]
}

def eval[T](e: Expr[T]): T = e match {
  case Expr.StrLit(s) => s
  case Expr.Pair(a, b) => (eval(a), eval(b))
}
```

We've also plugged a few soundness problems caused by inferring too much when matching on abstract, union and intersection types.


## Other changes
Some of the other notable changes include the following:

- Dotty is now fully boostrapped. This means that we are able to compile Dotty with Dotty itself, and hence use all the new features in the compiler code base.
- Singletones are now allowed in union types. E.g. the following is allowed: `object foo; type X = Int | foo.type`.
- A bunch of improvements was made for the type inference system – see, e.g., PRs [#6454](https://github.com/lampepfl/dotty/pull/6454) and [#6467](https://github.com/lampepfl/dotty/pull/6467).
- Improvements to the Scala 2 code support which, in particular, improves Cats support – see PRs [#6494](https://github.com/lampepfl/dotty/pull/6494) and [#6498](https://github.com/lampepfl/dotty/pull/6498).


# Let us know what you think!

If you have questions or any sort of feedback, feel free to send us a message on our
[Gitter channel](https://gitter.im/lampepfl/dotty). If you encounter a bug, please
[open an issue on GitHub](https://github.com/lampepfl/dotty/issues/new).

## Contributing

Thank you to all the contributors who made this release possible!

According to `git shortlog -sn --no-merges 0.14.0-RC1..0.15.0-RC1` these are:

```
TODO
```

If you want to get your hands dirty and contribute to Dotty, now is a good time to get involved!
Head to our [Getting Started page for new contributors](https://dotty.epfl.ch/docs/contributing/getting-started.html),
and have a look at some of the [good first issues](https://github.com/lampepfl/dotty/issues?q=is%3Aissue+is%3Aopen+label%3Aexp%3Anovice).
They make perfect entry points into hacking on the compiler.

We are looking forward to having you join the team of contributors.

## Library authors: Join our community build

Dotty now has a set of widely-used community libraries that are built against every nightly Dotty
snapshot. Currently this includes ScalaPB, algebra, scalatest, scopt and squants.
Join our [community build](https://github.com/lampepfl/dotty-community-build)
to make sure that our regression suite includes your library.

[Scastie]: https://scastie.scala-lang.org/?target=dotty

[@odersky]: https://github.com/odersky
[@DarkDimius]: https://github.com/DarkDimius
[@smarter]: https://github.com/smarter
[@felixmulder]: https://github.com/felixmulder
[@nicolasstucki]: https://github.com/nicolasstucki
[@liufengyun]: https://github.com/liufengyun
[@OlivierBlanvillain]: https://github.com/OlivierBlanvillain
[@biboudis]: https://github.com/biboudis
[@allanrenucci]: https://github.com/allanrenucci
[@Blaisorblade]: https://github.com/Blaisorblade
[@Duhemm]: https://github.com/Duhemm
[@AleksanderBG]: https://github.com/AleksanderBG
[@milessabin]: https://github.com/milessabin
[@anatoliykmetyuk]: https://github.com/anatoliykmetyuk
