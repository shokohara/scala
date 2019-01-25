package services

import java.util.UUID

import play.api.db.Database
import play.api.mvc.{AnyContent, MessagesRequest}
import play.api.libs.json.Json

import scala.util.{Random, Try}

object PriorConfirmation {
  def apply(db: Database): PriorConfirmation = new PriorConfirmation(db)
}
class PriorConfirmation(db: Database) extends DataBaseManager(db){
  import play.api.http.ContentTypes.JSON
  import play.api.http.HeaderNames.CONTENT_TYPE
  // TODO ったで検索して置換
  def signatureCheck(implicit request: MessagesRequest[AnyContent]): Either[String, UserRequestData] = for {
    _ <- request.headers.get(CONTENT_TYPE).toRight("header error").flatMap(a => Either.cond(a == JSON, a, "CONTENT_TYPE not json"))
    method <- Option(request.method).toRight("method error")
    key <- request.headers.get("AccessKey").toRight("AccessKey error")
    timestamp <- request.headers.get("AccessTimestamp").toRight("AccessTimestamp error")
    sign <- request.headers.get("AccessSign").toRight("AccessSign error")
    bodyAsRaw <- request.body.asRaw.toRight("asRawでNoneだった")
    bodyAsString <- bodyAsRaw.asBytes().map(_.utf8String).toRight("asBytesでNoneだった")
    bodyAsJson <- Try(Json.parse(bodyAsString)).toEither.left.map(_ => "Jsonではなかった")
    userData <- getUserData(key).toRight("cannot get userData")
    userId <- Certification.checkSign(userData, timestamp, method, request.path, bodyAsString, key, sign)
  } yield UserRequestData(userId, bodyAsJson)
  def createUserData(implicit request: MessagesRequest[AnyContent]): Either[DecodingFailure, UserData] = request.body.asJson.as[UserData]
  def createOrderData(userRequestData: UserRequestData): Either[String, OrderData] = {
    val body = userRequestData.requestBody
    (for{
      orderType <- (body \ "orderType").asOpt[String].toRight("orderType is Empty").flatMap(a => Either.cond(a == "LIMIT" || a == "MARKET", a, "orderType error"))
      side <- (body \ "side").asOpt[String].toRight("side is Empty").flatMap(a => Either.cond(a == "BUY" || a == "SELL", a, "side error"))
      price<- (body \ "price").asOpt[Int].toRight("price is Empty")
      size<- (body \ "size").asOpt[BigDecimal].toRight("size is Empty").flatMap(a => Either.cond(a > 0, a, "cannot set 0 to size"))
    } yield OrderData(orderType, side, price, size))
  }
  def createCancelOrderData(userRequestData: UserRequestData): Either[String, CancelOrderData] = {
    val body = userRequestData.requestBody
    (for{
      orderId <- (body \ "orderId").asOpt[String].toRight("orderId is Empty")
    } yield CancelOrderData(orderId))
  }
}


