package controllers

import javax.inject._
import play.api.mvc._
import services._

import play.api.db._

@javax.inject.Singleton
class Controller @Inject()(db: Database, mcc: MessagesControllerComponents) extends MessagesAbstractController(mcc) {
  val priorConfirmation = PriorConfirmation(db)
  val orderManager = OrderManager(db)
  def index() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok("Top Page!")
  }
  def addUser() = Action { implicit request: MessagesRequest[AnyContent] =>
    val r = (for {
      userData <- priorConfirmation.createUserData
      r <- orderManager.addUser(userData)
    } yield r).merge
    println(r)
    Ok(r)
  }
  def notFound(hoge: String) = Action { implicit request: MessagesRequest[AnyContent] =>
    NotFound(s"""{"error" : "${request} NotFound!"}""")
  }
}