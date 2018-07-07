package radar
package model

case class Message(
  id               : Option[Int] = None
, link             : String
, text             : String
, format           : String
, target           : Option[String]
, created_at       : Long = time.now
, notification_sent: Boolean = false)
