package org.hablapps.stateless
package core
package nat

import scalaz.{ Monad, ~> }

trait OpticAlg[P[_], A, Ev[M[_], _] <: Monad[M], F[_]] extends Monad[P] {

  type Q[_]

  implicit val ev: Ev[Q, A]

  val hom: Q ~> λ[x => P[F[x]]]
}
