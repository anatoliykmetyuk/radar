package radar
package db

import cats._, cats.implicits._, cats.effect._, cats.data._
import doobie._, doobie.implicits._

import infrastructure.tr
import radar.model._

object credentials extends CredentialsHelpers {
  def get(target: String): IO[Credentials] =
    (selectSql ++ sql"""where target = $target""")
      .query[Credentials].unique.transact(tr)

  def create(c: Credentials): IO[Int] = {
    import c._
    sql"""
      insert into credentials (
        target
      , login
      , password)
      values (
        $target
      , $login
      , $password)
      """
      .update.withUniqueGeneratedKeys[Int]("id").transact(tr)
    }
}

trait CredentialsHelpers {
  val selectSql =
    fr"""
      select
        id
      , target
      , login
      , password
      from credentials"""
}
