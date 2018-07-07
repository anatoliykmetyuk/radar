package radar
package model

case class Credentials(
  id      : Option[Int] = None
, target  : String
, login   : String
, password: String)