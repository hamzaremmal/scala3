/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc. dba Akka
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.runtime.java8

import scala.language.`2.13`

@FunctionalInterface trait JFunction2$mcVIJ$sp extends Function2[Any, Any, Any] with Serializable {
  def apply$mcVIJ$sp(v1: Int, v2: Long): Unit
  override def apply(v1: Any, v2: Any): Any = {
    apply$mcVIJ$sp(scala.runtime.BoxesRunTime.unboxToInt(v1), scala.runtime.BoxesRunTime.unboxToLong(v2))
    scala.runtime.BoxedUnit.UNIT
  }
}
