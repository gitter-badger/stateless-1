package org.hablapps.stateless
package core
package nat
package doobieImpl

import scalaz._, Scalaz._
import doobie.imports._
import monocle.{Lens => MLens}

object OptionalDoobie{

  def fromState[K,V](dopt: DoobieOptional[K,V]) =
    OptionalAlg[StateT[ConnectionIO,K,?],State[V,?],V]{
      new (State[V,?] ~> λ[T => StateT[ConnectionIO,K,Option[T]]]){
        def apply[T](q: State[V,T]): StateT[ConnectionIO,K,Option[T]] =
          StateT[ConnectionIO,K,Option[T]]{
            k: K => dopt.getOption(k).unique >>= { 
              _.fold((k, Option.empty[T]).point[ConnectionIO])(
                q(_) match {
                  case (v,out) => dopt.set(k)(v).run.as((k,Some(out)))
                })
              }
          }
        }
    }(StateT.stateTMonadState[K,ConnectionIO], implicitly)

  def fromStateT[K1,K2](dopt: DoobieOptional[K1,K2]) =
    OptionalAlg[StateT[ConnectionIO,K1,?],StateT[ConnectionIO,K2,?],K2]{
      new (StateT[ConnectionIO,K2,?] ~> λ[T=>StateT[ConnectionIO,K1,Option[T]]]){
        def apply[T](q: StateT[ConnectionIO,K2,T]): StateT[ConnectionIO,K1,Option[T]] =
          StateT[ConnectionIO,K1,Option[T]]{
            k: K1 => dopt.getOption(k).unique >>= { 
              _.fold((k,Option.empty[T]).point[ConnectionIO]){
                q(_) >>= {
                  case (v,out) => dopt.set(k)(v).run.as((k,Some(out)))
                }
              }
            }
          }
      }
    }(StateT.stateTMonadState[K1,ConnectionIO],StateT.stateTMonadState[K2,ConnectionIO])

}
