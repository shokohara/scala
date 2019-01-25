package services

import play.api.libs.json.JsValue

// フィールド名にcamel caseを使う
// https://github.com/jilen/play-circe
case class UserData(user_id: String, val user_name: String, val key: String, val secret: String, val asset: Long)
case class UserRequestData(val userId: String, requestBody: JsValue)

sealed abstract class OrderType(val value: String) extends StringEnumEntry

case object OrderType extends StringEnum[OrderType] with StringCirceEnum[OrderType]{
  case object Limit extends OrderType("LIMIT")
  case object Market   extends OrderType("MARKET")
  val values = findValues
}
// OrderTypeにhttps://github.com/lloydmeta/enumeratumを使う
case class OrderData(val orderType: OrderType, val side: String, val price: Int Refined NonNegative, val size: BigDecimal)
object OrderData{
  implicit val encoder = deriveEncoder[OrderData]
  implicit val decoder = deriveDecoder[OrderData]
}
case class CancelOrderData(val orderId: String)
case class OrderBoardData(val user_id: String, val timestamp: BigDecimal, val side: String, val price: Int, val size: BigDecimal, val orderId: String)

