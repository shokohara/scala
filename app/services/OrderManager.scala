package services

import java.util.UUID

import org.json4s._
import org.json4s.jackson.JsonMethods._
import play.api.db._

object OrderManager {
  def apply(db: Database): OrderManager = new OrderManager(db)
}
class OrderManager (db: Database) extends DataBaseManager(db) {
  def order (orderData: OrderData)(implicit userId: String): Either[String, String] = {
    val orderId = createOrderId
    val timestamp = createTimeStamp
    orderData.orderType match {
      case "LIMIT" => limitOrder(orderId, timestamp)(orderData)
      case "MARKET" => marketOrder(orderId, timestamp)(orderData)
    }
  }
  def limitOrder(orderId: String, timestamp: BigDecimal)(orderData: OrderData)(implicit userId: String): Either[String, String] = {
    val filter = if (orderData.side == "BUY") {(boardPrice: Int) => {boardPrice <= orderData.price}} else (boardPrice: Int) => {boardPrice >= orderData.price}
    for{
      contractDataTuple <- getContractDataTuple(filter)(orderData)
      _ <- runContractData(orderId, timestamp)(contractDataTuple._2)
      _ <- setOrderToBoard(orderId, timestamp)(OrderData(orderData.orderType, orderData.side, orderData.price, contractDataTuple._1))
    } yield s"""{"orderId":"${orderId}"}"""
  }
  def marketOrder(orderId: String, timestamp: BigDecimal)(orderData: OrderData)(implicit userId: String): Either[String, String] = {
    for{
      contractDataTuple <- getContractDataTuple((_: Int) => true)(orderData)
      _ <- runContractData(orderId, timestamp)(contractDataTuple._2)
    } yield s"""{"orderId":"${orderId}"}"""
  }
  def runContractData(orderId: String, timestamp: BigDecimal)(contractOrderList: List[OrderBoardData])(implicit userId: String): Either[String, Int] = {
    val (insertQueryString, updateQueryString) = contractOrderList.foldLeft(("", ""))((queryStringTuple, contractData) => {
      (queryStringTuple._1 + recordTransaction(orderId, timestamp)(contractData), queryStringTuple._2 + updateBoard(contractData))
    })
    if (insertQueryString == "" | updateQueryString == "") Right(0) else {
      for{
        _ <- insert(insertQueryString)
        a <- update(updateQueryString)
      } yield a
    }
  }
  def updateBoard(contractData: OrderBoardData): String = {
    if (contractData.timestamp == 0) s"UPDATE Trade_Order SET size = ${contractData.size} WHERE orderId = '${contractData.orderId}';" else s"DELETE from Trade_Order WHERE orderId = '${contractData.orderId}';"
  }
  def recordTransaction(orderId: String, timestamp: BigDecimal)(contractData: OrderBoardData)(implicit userId: String): String = {
    //taker側
    //板側
    //履歴
    s"INSERT INTO `Trade_Open_Position` VALUES ('${contractData.user_id}', $timestamp, '${contractData.side}', '${contractData.price}', ${contractData.size}, '${contractData.orderId}');" +
    s"INSERT INTO `Trade_Open_Position` VALUES ('$userId', $timestamp, '${if (contractData.side == "BUY") "SELL" else "BUY"}', '${contractData.price}', ${contractData.size}, '$orderId');" +
    s"INSERT INTO `Trade_Execution` VALUES ('$createId', $timestamp, '${if (contractData.side == "BUY") "SELL" else "BUY"}', '${contractData.price}', ${contractData.size}, '$orderId', '${contractData.orderId}');"
  }
  def setOrderToBoard(orderId: String, timestamp: BigDecimal)(orderData: OrderData)(implicit userId: String): Either[String, Int] = {
    insert(s"INSERT INTO `Trade_Order` VALUES ('$userId', $timestamp, '${orderData.side}', ${orderData.price}, ${orderData.size}, '$orderId')")
  }

  def updateUserAsset(profits: BigDecimal)(implicit userId: String): String = {
    //未実装
    s"UPDATE Trade_User SET asset = asset + ${profits} WHERE user_id = '${userId}';"
  }
  def getContractDataTuple(filter: Int => Boolean)(orderData: OrderData): Either[String, (BigDecimal, List[OrderBoardData])] = (for{
    boardList <- (if (orderData.side == "BUY") getSellBoard() else getBuyBoard()).toRight("cannot get orderBoardData")
  } yield {
    boardList.foldLeft((orderData.size, List(): List[OrderBoardData]))((t, orderBoardData) => {
      if (filter(orderBoardData.price)) {
        (t._1 match {
          case s if s == 0 => t
          case s if s >= orderBoardData.size =>
            (s - orderBoardData.size, orderBoardData +: t._2)
          case s if s < orderBoardData.size =>
            (0, OrderBoardData(orderBoardData.user_id, 0, orderBoardData.side, orderBoardData.price, s, orderBoardData.orderId) +: t._2)
        })
      } else t
    })
  })

  def cancelOrder(userId: String, cancelOrderData: CancelOrderData): Either[String, CancelOrderData] = {
    update(s"DELETE from Trade_Order WHERE child_order_acceptance_id = '${cancelOrderData.orderId}'").map(_ => cancelOrderData)
  }
  def cancelAllOrder(userId: String): Either[String, String] = {
    update(s"DELETE from Trade_Order WHERE user_id = '$userId'").right.map(_ => "success")
  }
  def createId(): String = {
    UUID.randomUUID().toString filterNot(_ == '-')
  }
  def createOrderId(): String = {
    UUID.randomUUID().toString filterNot(_ == '-')
  }
  def createTimeStamp(): BigDecimal = {
    BigDecimal(123456)
  }
}