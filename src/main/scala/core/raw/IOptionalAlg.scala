package org.hablapps.stateless
package core
package raw

import scalaz.Monad

trait IOptionalAlg[P[_], I, A] extends Monad[P] {

  def getOption: P[Option[(I, A)]]

  def setOption(a: A): P[Option[Unit]]
}