package org.hablapps.stateless
package test
package university

import org.scalatest._

import scalaz.{ MonadError, \/ }

import monocle.macros.Lenses
import monocle.function.all._

import smonocle.nat.all._

import org.hablapps.puretest, puretest._, puretest.Filter.Syntax._

import StateTests._

class StateTests extends FunSpec with Matchers with MyTest[Program, SUniversity] {

  implicit val MonadErrorP =
    stateTMonadError[SUniversity, Throwable \/ ?, Throwable]

  lazy val uni: University[Program, SUniversity] = SUniversity.model

  val FilterP: puretest.Filter[Program] = puretest.Filter[Program]

  val Tester: StateTester[Program, SUniversity, Throwable] =
    StateTester.StateTStateTester[Throwable, Throwable \/ ?, SUniversity]
}

import scalaz._, Scalaz._

object StateTests {
  type Program[A] = StateT[Throwable \/ ?, SUniversity, A]
  type DProgram[A] = StateT[Throwable \/ ?, SDepartment, A]
  type AProgram[A] = StateT[Throwable \/ ?, Option[SDepartment], A]
  type LProgram[A] = StateT[Throwable \/ ?, SLecturer, A]
}

@Lenses case class SUniversity(name: String, departments: Map[String, SDepartment])
@Lenses case class SDepartment(budget: Int, lecturers: List[SLecturer])
@Lenses case class SLecturer(firstName: String, lastName: String, salary: Int)

object SUniversity {
  import core.nat.LensAlg, core.nat.op.At

  def model: University.Aux[Program, SUniversity, AProgram, DProgram, SDepartment] =
    University[Program, SUniversity, AProgram, DProgram, SDepartment](
      asLensField[Throwable \/ ?, SUniversity, String](SUniversity.name),
      SDepartment.model,
      fromTraversal[Throwable \/ ?, SUniversity, SDepartment](
        SUniversity.departments.composeTraversal(each)),
      fromAtStateT[Throwable \/ ?, SUniversity, String, SDepartment](
        asLensAlg[Throwable \/ ?, SUniversity, Map[String, SDepartment]](
          SUniversity.departments)),
      name => new SUniversity(name, Map.empty))
}

object SDepartment {

  def model: Department.Aux[DProgram, SDepartment, LProgram, SLecturer] =
    Department[DProgram, SDepartment, LProgram, SLecturer](
      SLecturer.model,
      fromTraversal[Throwable \/ ?, SDepartment, SLecturer](
        SDepartment.lecturers.composeTraversal(each)),
      asLensField[Throwable \/ ?, SDepartment, Int](SDepartment.budget),
      (i, xs) => SDepartment(i, xs.map((SLecturer.apply _).tupled)))
}

object SLecturer {

  def model: Lecturer[LProgram, SLecturer] =
    Lecturer[LProgram, SLecturer](
      asLensField[Throwable \/ ?, SLecturer, String](SLecturer.firstName),
      asLensField[Throwable \/ ?, SLecturer, String](SLecturer.lastName),
      asLensField[Throwable \/ ?, SLecturer, Int](SLecturer.salary),
      (f, l, s) => SLecturer(f, l, s))
}