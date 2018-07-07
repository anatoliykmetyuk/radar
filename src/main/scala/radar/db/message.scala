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
        $link
      , $text
      , $format
      , $target
      , to_timestamp($created_at / 1000)
      , $notification_sent)
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

  def listNew(expirationDays: Double = 3.0): IO[List[Message]] =
    (selectSql ++ sql"""
      where notification_sent = false and
      (extract(epoch from current_timestamp-created_at)/(86400::float4)) < $expirationDays
      order by created_at desc
    """)
    .query[Message].to[List].transact(tr)

  def listLatest(format: String, target: Option[String] = None, take: Option[Int] = None): IO[List[Message]] =
    (target match {
      case Some(t) =>
        (selectSql ++ sql"""
          where format = $format and target = $t
          order by created_at desc limit $take
        """)
      
      case None =>
        (selectSql ++ sql"""
          where format = $format
          order by created_at desc limit $take
        """)
    }).query[Message].to[List].transact(tr)  // TODO DRY

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
