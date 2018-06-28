import cats._, cats.implicits._

package object radar {
  type Ef[A] = Either[String, A]

  def opt[A](o: Option[A], msg: String = const.err.emptyOption): Ef[A] =
    o.map(x => Right[String, A](x)).getOrElse(Left[String, A](msg))

  def exn[A, E <: Throwable](e: Either[E, A]): Ef[A] =
    e.leftMap(_.getMessage)

  def run[A](ef: Ef[A]): A = ef match {
    case Right(a) => a
    case Left (e) => throw new RuntimeException(e)
  }
}
