package radar
package db

import cats._, cats.implicits._, cats.effect._, cats.data._
import doobie._, doobie.implicits._

import infrastructure.tr
import radar.model._

object credentials extends CredentialsHelpers{
  def get(target: String, key: String): Ef[Credentials] =
    for {
      cr <- ioe { (selectSql ++ sql"""where target = $target""")
              .query[Credentials].unique.transact(tr) }
      res <- decrypt(cr, key)
    } yield res
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

  def decrypt(what: Credentials, k: String): Ef[Credentials] = ???
}
