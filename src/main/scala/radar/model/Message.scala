package radar
package model

case class Message(
  id               : Option[Int] = None
, link             : String
, text             : String
, format           : String
, target           : Option[String] = None
, created_at       : Long = time.now
, notification_sent: Boolean = false) {

  override def toString() =
    s"${format}${target.map(t => s":$t").getOrElse("")}\n$text\n$link"

  override def equals(that: Any): Boolean = that match {
    case x: Message => x.link == link
    case _ => false
  }

  override def hashCode(): Int = link.hashCode
}
