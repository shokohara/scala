package services

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HMACSHA256 {
  def apply(text: String, sharedSecret: String): String = {
    val algorithm = "HMacSha256"
    val secret = new SecretKeySpec(sharedSecret.getBytes, algorithm)
    val mac = Mac.getInstance(algorithm)
    mac.init(secret)
    mac.doFinal(text.getBytes).foldLeft("")((l, r) => l + Integer.toString((r & 0xff) + 0x100, 16).substring(1))
  }
}