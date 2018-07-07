package radar
package model

case class Credentials(
  id      : Option[Int] = None
, target  : String
, login   : String
, password: String) {
  def encrypted(key: String): Credentials = copy(
    login    = crypto.encrypt(login, key)
  , password = crypto.encrypt(password, key))

  def decrypted(key: String): Credentials = copy(
    login    = crypto.decrypt(login, key)
  , password = crypto.decrypt(password, key))
}
