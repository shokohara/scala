package services

object Certification {

  def add[A: Monoid](a :A, b:A): A = a |+| b

  def add(a:Int,b:Int):Int = a + b

  def checkSign(userData: UserData, timestamp: String, method: String, path: String, body: String, key: String, sign: String): Either[String, String] = {
    if (HMACSHA256(timestamp + method + path + body, userData.secret) == sign) Right(userData.user_id) else Left("signature error")
  }
}
