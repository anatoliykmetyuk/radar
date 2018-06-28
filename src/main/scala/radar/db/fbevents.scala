package radar
package db

import cats._, cats.implicits._, cats.effect._, cats.data._
import doobie._, doobie.implicits._

import infrastructure.tr

object fbevents extends FbEventsDbHelpers {
  def create(evt: FacebookEvent): IO[Int] = {
    import evt._
    sql"""
      insert into fbevents(
        month
      , "date"
      , name
      , link
      , details
      , created)
      values (
        $month
      , $date
      , $name
      , $link
      , $details
      , to_timestamp($created / 1000))
      """
      .update.withUniqueGeneratedKeys[Int]("id").transact(tr)
  }

  def list: IO[List[FacebookEvent]] =
    selectSql.query[FacebookEvent].to[List].transact(tr)

  def listLatest(lim: Int): IO[List[FacebookEvent]] =
    (selectSql ++ sql"order by id desc limit $lim")
      .query[FacebookEvent].to[List].transact(tr)

  def get(id: Int): IO[FacebookEvent] =
    (selectSql ++ sql"""where id = $id""")
      .query[FacebookEvent].unique.transact(tr)
}

trait FbEventsDbHelpers {
  val selectSql =
    fr"""
      select
        id
      , month
      , "date"
      , name
      , link
      , details
      , extract(epoch from created) * 1000
      from fbevents"""
}
