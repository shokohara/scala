package services

import play.api.libs.json.JsValue

case class UserData(val user_id: String, val user_name: String, val key: String, val secret: String, val asset: Long)
case class UserRequestData(val userId: String, requestBody: JsValue)
case class OrderData(val orderType: String, val side: String, val price: Int, val size: BigDecimal)
case class CancelOrderData(val orderId: String)
case class OrderBoardData(val user_id: String, val timestamp: BigDecimal, val side: String, val price: Int, val size: BigDecimal, val orderId: String)

