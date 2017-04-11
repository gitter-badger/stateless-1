package org.hablapps.phoropter
package test

import org.scalatest._

import scalaz._, Scalaz._

import monocle.macros.Lenses

import smonocle.symmetric.nat.all.{ fromLens => fromSLens }
import smonocle.asymmetric.nat.all.fromLens

class SLensAlgTest extends FlatSpec with Matchers {

  @Lenses
  case class Person(name: String, last: String, age: Int)

  val ageSln = fromSLens[Id, Person, Int](Person.age)

  val doe = Person("John", "Doe", 40)
  val wick = Person("John", "Wick", 45)

  "SLens" should "get" in {
    ageSln.getL.apply(doe) shouldBe (doe, doe)
    ageSln.getR.apply(doe) shouldBe (doe, doe.age)
  }

  it should "modify" in {
    ageSln.setL(wick).apply(doe) shouldBe (wick, ())
    ageSln.setR(50).apply(doe) shouldBe (doe.copy(age = 50), ())
  }

  @Lenses
  case class Department(name: String, boss: Person)

  val bossLn = fromLens[Id, Department, Person](Department.boss)

  val department = Department("IT", doe)

  it should "compose with asymmetric lens" in {
    val newSln =
      bossLn.composeSLens[State[Person, ?], State[Int, ?], Person, Int](ageSln)

    newSln.getL.apply(department) shouldBe (department, doe)
    newSln.getR.apply(department) shouldBe (department, doe.age)

    newSln.setL(wick).apply(department) shouldBe
      (department.copy(boss = wick), ())
    newSln.setR(50).apply(department) shouldBe
      (department.copy(boss = doe.copy(age = 50)), ())
  }
}