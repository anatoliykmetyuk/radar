package radar

case class Message(
  id      : Option[Int] = None
, message : String
, link    : String
, protocol: String  // TODO make it enum
, target  : String
, created : Long = time.now)
