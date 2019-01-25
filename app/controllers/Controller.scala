package controllers

import javax.inject._
import play.api.db._
import play.api.mvc._
import services._

@javax.inject.Singleton
class Controller @Inject()(db: Database, mcc: MessagesControllerComponents) extends MessagesAbstractController(mcc) {
  val priorConfirmation = PriorConfirmation(db)
  val orderManager = OrderManager(db)
  def index() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok("Top Page!")
  }
  def addUser() = Action(circe.json[UserData]) { implicit request: MessagesRequest[AnyContent] =>
    orderManager.addUser(request.body).map(r => Ok(r))
  }
  def notFound(hoge: String) = Action { implicit request: MessagesRequest[AnyContent] =>
    NotFound(s"""{"error" : "${request} NotFound!"}""")
  }
}