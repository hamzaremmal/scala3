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

@FunctionalInterface trait JFunction0$mcD$sp extends Function0[Any] with Serializable {
  def apply$mcD$sp(): Double
  override def apply(): Any = scala.runtime.BoxesRunTime.boxToDouble(apply$mcD$sp())
}
