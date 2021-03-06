import scala.util.Try
import cats._, cats.implicits._, cats.effect._, cats.data.{ NonEmptyList => NEL, _ }

package object radar {
  type Ef[A] = EitherT[IO, NEL[String], A]

  /** IO Effect */
  def ioe[A](x: IO[A]): Ef[A] = EitherT[IO, NEL[String], A](x.map(Right(_)))

  /** Option */
  def opt[A](o: Option[A], msg: String = const.err.emptyOption): Ef[A] = o
    .map { x =>
      EitherT(IO[Either[NEL[String], A]] { Right[NEL[String], A]( x             ) }) }.getOrElse {
      EitherT(IO[Either[NEL[String], A]] { Left [NEL[String], A]( NEL(msg, Nil) ) }) }

  /** Exception under Either */
  def exn[A, E <: Throwable](e: Either[E, A]): Ef[A] =
    EitherT(IO[Either[NEL[String], A]] { e.leftMap(x => NEL(x.getMessage, Nil)) })

  /** Attempt to run an error-prone computation */
  def att[A](a: => A): Ef[A] = exn { Try(a).toEither }

  /** Extract A out of Ef[A], run all side effects */
  def run[A](ef: Ef[A]): A = ef.value.unsafeRunSync match {
    case Right(a) => a
    case Left (e) =>
      println(s"The following errors happened:\n${e.toList.mkString("\n")}")
      throw new RuntimeException("Ef execution error")
  }
}
