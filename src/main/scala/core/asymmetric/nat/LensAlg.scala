package org.hablapps.phoropter
package core
package asymmetric
package nat

import scalaz.{ Const, Monad, MonadState, ~> }
import scalaz.Id.Id

import symmetric.nat._

trait LensAlg[P[_], Q[_], A] extends OpticAlg[P, Q, A, MonadState, Id]
    with raw.LensAlg[P, A] {

  override def get: P[A] = hom[A](ev.get)

  override def put(a: A): P[Unit] = hom(ev.put(a))

  /* composing algebras */

  def composeFold[R[_], B](fl: FoldAlg[Q, R, B]): FoldAlg[P, R, B] =
    FoldAlg(λ[R ~> λ[x => P[List[x]]]](rx => hom(fl.hom(rx))))(this, fl.ev)

  def composeGetter[R[_], B](gt: GetterAlg[Q, R, B]): GetterAlg[P, R, B] =
    GetterAlg(hom compose gt.hom)(this, gt.ev)

  def composeSetter[R[_], B](st: SetterAlg[Q, R, B]): SetterAlg[P, R, B] =
    SetterAlg(λ[R ~> λ[x => P[Const[Unit, x]]]](rx => hom(st.hom(rx))))(this, st.ev)

  def composeTraversal[R[_], B](tr: TraversalAlg[Q, R, B]): TraversalAlg[P, R, B] =
    TraversalAlg(λ[R ~> λ[x => P[List[x]]]](rx => hom(tr.hom(rx))))(this, tr.ev)

  def composeOptional[R[_], B](op: OptionalAlg[Q, R, B]): OptionalAlg[P, R, B] =
    OptionalAlg(λ[R ~> λ[x => P[Option[x]]]](rx => hom(op.hom(rx))))(this, op.ev)

  def composePrism[R[_], B](pr: PrismAlg[Q, R, B]): OptionalAlg[P, R, B] =
    OptionalAlg(λ[R ~> λ[x => P[Option[x]]]](rx => hom(pr.hom(rx))))(this, pr.ev)

  def composeLens[R[_], B](ln: LensAlg[Q, R, B]): LensAlg[P, R, B] =
    LensAlg(hom compose ln.hom)(this, ln.ev)

  /* composing symmetric algebras */

  def composeSFold[L[_], R[_], B, C](fl: SFoldAlg[Q, L, R, B, C]): SFoldAlg[P, L, R, B, C] =
    SFoldAlg(
      λ[L ~> λ[x => P[List[x]]]](rx => hom(fl.homL(rx))),
      λ[R ~> λ[x => P[List[x]]]](rx => hom(fl.homR(rx))))(this, fl.evL, fl.evR)

  def composeSGetter[L[_], R[_], B, C](gt: SGetterAlg[Q, L, R, B, C]): SGetterAlg[P, L, R, B, C] =
    SGetterAlg(hom compose gt.homL, hom compose gt.homR)(this, gt.evL, gt.evR)

  def composeSSetter[L[_], R[_], B, C](st: SSetterAlg[Q, L, R, B, C]): SSetterAlg[P, L, R, B, C] =
    SSetterAlg(
      λ[L ~> λ[x => P[Const[Unit, x]]]](rx => hom(st.homL(rx))),
      λ[R ~> λ[x => P[Const[Unit, x]]]](rx => hom(st.homR(rx))))(this, st.evL, st.evR)

  def composeSTraversal[L[_], R[_], B, C](tr: STraversalAlg[Q, L, R, B, C]): STraversalAlg[P, L, R, B, C] =
    STraversalAlg(
      λ[L ~> λ[x => P[List[x]]]](rx => hom(tr.homL(rx))),
      λ[R ~> λ[x => P[List[x]]]](rx => hom(tr.homR(rx))))(this, tr.evL, tr.evR)

  def composeSOptional[L[_], R[_], B, C](op: SOptionalAlg[Q, L, R, B, C]): SOptionalAlg[P, L, R, B, C] =
    SOptionalAlg(
      λ[L ~> λ[x => P[Option[x]]]](rx => hom(op.homL(rx))),
      λ[R ~> λ[x => P[Option[x]]]](rx => hom(op.homR(rx))))(this, op.evL, op.evR)

  def composeSPrism[L[_], R[_], B, C](pr: SPrismAlg[Q, L, R, B, C]): SOptionalAlg[P, L, R, B, C] =
    SOptionalAlg(
      λ[L ~> λ[x => P[Option[x]]]](rx => hom(pr.homL(rx))),
      λ[R ~> λ[x => P[Option[x]]]](rx => hom(pr.homR(rx))))(this, pr.evL, pr.evR)

  def composeSLens[L[_], R[_], B, C](ln: SLensAlg[Q, L, R, B, C]): SLensAlg[P, L, R, B, C] =
    SLensAlg(hom compose ln.homL, hom compose ln.homR)(this, ln.evL, ln.evR)
}

object LensAlg {

  def apply[P[_], Q[_], A](
      hom2: Q ~> P)(implicit
      ev0: Monad[P],
      ev1: MonadState[Q, A]) = new LensAlg[P, Q, A] {
    def point[X](x: => X) = ev0.point(x)
    def bind[X, Y](fx: P[X])(f: X => P[Y]): P[Y] = ev0.bind(fx)(f)
    implicit val ev = ev1
    val hom = hom2
  }
}