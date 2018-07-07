package radar
package db

import cats._, cats.implicits._, cats.effect._, cats.data._
import doobie._, doobie.implicits._

import infrastructure.tr
import radar.model._


object message extends MessageHelpers {
  def create(evt: Message): IO[Int] = {
    import evt._
    sql"""
      insert into message(
        link
      , "text"
      , format
      , target
      , created_at
      , notification_sent)
      values (
        link
      , text
      , format
      , target
      , to_timestamp($created_at / 1000)
      , notification_sent)
      """
      .update.withUniqueGeneratedKeys[Int]("id").transact(tr)
  }

  def markNotified(id: Int): IO[Int] =
    sql"""
      update message set notification_sent = true
      where id = $id
    """.update.run.transact(tr)

  def list: IO[List[Message]] =
    selectSql.query[Message].to[List].transact(tr)

  def listNew(take: Option[Int] = None, expiration: Option[Double] = None): IO[List[Message]] =
    (selectSql ++ sql"""
      where notification_sent = false and
      (extract(epoch from current_timestamp-created_at)/(86400::float4)) < ${expiration.getOrElse(Double.MaxValue)}
      order by created_at asc limit ${take.getOrElse(Int.MaxValue)}
    """)
    .query[Message].to[List].transact(tr)

  def get(id: Int): IO[Message] =
    (selectSql ++ sql"""where id = $id""")
      .query[Message].unique.transact(tr)
}

trait MessageHelpers {
  val selectSql =
    fr"""
    select
      id
    , link
    , "text"
    , format
    , target
    , extract(epoch from created_at) * 1000
    , notification_sent
    from message"""
}
