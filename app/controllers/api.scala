package controllers.api

import javax.inject._
import play.api.db._
import play.api.mvc._

import services._

@javax.inject.Singleton
class Controller @Inject()(db: Database, mcc: MessagesControllerComponents) extends MessagesAbstractController(mcc) {
  val priorConfirmation = PriorConfirmation(db)
  val orderManager = OrderManager(db)
  def signTest() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok((for(_ <- priorConfirmation.signatureCheck) yield "success").merge)
  }
  def order() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok((for {
      userRequestData <- priorConfirmation.signatureCheck
      orderData <- priorConfirmation.createOrderData(userRequestData)
      r <- orderManager.order(orderData)(userRequestData.userId)
    } yield r).merge)
  }
  def cancelOrder() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok((for {
      userRequestData <- priorConfirmation.signatureCheck
      cancelOrderData <- priorConfirmation.createCancelOrderData(userRequestData)
      r <- orderManager.cancelOrder(userRequestData.userId, cancelOrderData)
    } yield r.orderId).merge)
  }
  def cancelAllOrder() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok((for {
      userRequestData <- priorConfirmation.signatureCheck
      r <- orderManager.cancelAllOrder(userRequestData.userId)
    } yield r).merge)
  }
}
