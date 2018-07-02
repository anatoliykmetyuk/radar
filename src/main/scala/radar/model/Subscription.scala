package radar.model

case class Subscription(
  id         : Option[Int] = None
, subscriber : Int
, protocol   : String
, target     : Option[String] = None
, notifyOwner: Boolean = true)
