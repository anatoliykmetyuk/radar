package radar

import javax.crypto.Cipher
import javax.crypto.spec.{ IvParameterSpec, SecretKeySpec }

object crypto {
  val enc = "utf8"

  val cipherName = "DES"
  val cipherTransformation = "DES/CBC/PKCS5Padding"

  // https://stackoverflow.com/questions/1205135/how-to-encrypt-string-in-java
  def encrypt(str: String, key: String): String = {
    // Let's assume the bytes to encrypt are in
    val input = str.getBytes(enc)

    val cipher = createCipher(Cipher.ENCRYPT_MODE, key)

    val encrypted = new Array[Byte](cipher.getOutputSize(input.length))
    var enc_len   = cipher.update(input, 0, input.length, encrypted, 0)
    enc_len += cipher.doFinal(encrypted, enc_len)

    // Transform bytes to text
    javax.xml.bind.DatatypeConverter.printHexBinary(encrypted)
  }

  def decrypt(hex: String, key: String): String = {
    val encrypted = javax.xml.bind.DatatypeConverter.parseHexBinary(hex)
    val enc_len = encrypted.length

    val cipher = createCipher(Cipher.DECRYPT_MODE, key)
    val decrypted = new Array[Byte](cipher.getOutputSize(enc_len))
    var dec_len = cipher.update(encrypted, 0, enc_len, decrypted, 0)
    dec_len += cipher.doFinal(decrypted, dec_len)

    new String(decrypted, enc)
  }

  def createCipher(mode: Int, keyRaw: String): Cipher = {
    // Next, you'll need the key and initialization vector bytes
    val keyBytes = makeKey(keyRaw)
    val ivBytes  = makeKey(keyRaw.reverse)

    // Now you can initialize the Cipher for the algorithm that you select:
    // wrap key data in Key/IV specs to pass to cipher
    val key    = new SecretKeySpec(keyBytes, cipherName)
    val ivSpec = new IvParameterSpec(ivBytes)
    // create the cipher with the algorithm you choose
    // see javadoc for Cipher class for more info, e.g.
    val cipher = Cipher.getInstance(cipherTransformation)

    // Encryption would go like this:
    cipher.init(mode, key, ivSpec)
    cipher
  }

  def makeKey(str: String, length: Int = 8): Array[Byte] = {
    val original = str.getBytes(enc).toList
    def loop[A](xs: List[A], orig: List[A]): Stream[A] = xs match {
      case x :: xss => x #:: loop(xss, orig)
      case Nil      => loop(orig, orig)
    }
    loop(original, original)
      .take(length).toArray
  }
}
