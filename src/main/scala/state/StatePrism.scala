package org.hablapps.phoropter
package state

import scalaz.{ Monad, StateT, ~> }
import StateT.stateTMonadState
import scalaz.syntax.monad._

import monocle.Prism

import core.MonadPrism

trait StatePrism {

  def fromPrism[F[_]: Monad, S, A](
      pr: Prism[S, A]): MonadPrism[StateT[F, S, ?], StateT[F, A, ?], A] = {
    new MonadPrism[StateT[F, S, ?], StateT[F, A, ?], A] {

      def point[X](x: => X) = stateTMonadState.point(x)

      def bind[X, Y](fx: StateT[F, S, X])(f: X => StateT[F, S, Y]) =
        stateTMonadState.bind(fx)(f)

      implicit val MS = stateTMonadState

      val hom = new (StateT[F, A, ?] ~> λ[x => StateT[F, S, Option[x]]]) {
        def apply[X](sa: StateT[F, A, X]): StateT[F, S, Option[X]] =
          StateT(s => pr.getOption(s).map(sa.run).fold(
            (s, Option.empty[X]).point[F])(
            _.map { case (a, o) => (pr.reverseGet(a), Option(o)) }))
      }
    }
  }
}
