package services

import anorm.SqlParser._
import anorm._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import play.api.db.Database

object DataBaseManager {
  def apply(db: Database): DataBaseManager = new DataBaseManager(db)
}
class DataBaseManager (val db: Database) {
  def addUser(userData: UserData): Either[String, String] = {
    insert(s"INSERT INTO `Trade_User` VALUES ('${userData.user_id}', '${userData.user_name}', '${userData.key}', '${userData.secret}', ${userData.asset})").map(_ => s"""{"key":"${userData.key}", "secret":"${userData.secret}"}""")
  }
  def getUserData(key: String): Option[UserData] = {
    val p: ResultSetParser[List[UserData]] = {
      (get[String]("user_id") ~ get[String]("user_name") ~ get[String]("apikey") ~ get[String]("apisecret") ~ get[Long]("asset") map {
        case a ~ b ~ c ~ d ~ e => UserData(a, b, c, d, e)
      })*
    }
    getData[List[UserData]](s"select * from Trade_User WHERE apikey = '${key}' ")(p).flatMap(_.headOption)
  }
  def getBuyBoard(): Option[List[OrderBoardData]] = {
    val p: ResultSetParser[List[OrderBoardData]] = {
      (get[String]("user_id") ~ get[BigDecimal]("timestamp") ~ get[String]("side") ~ get[Int]("price") ~ get[BigDecimal]("size") ~ get[String]("orderId") map {
        case a ~ b ~ c ~ d ~ e ~ f => OrderBoardData(a, b, c, d, e, f)
      })*
    }
    getData[List[OrderBoardData]](s"select * from Trade_Order WHERE side = 'BUY' ORDER BY price DESC, timestamp ASC")(p)
  }
  def getSellBoard(): Option[List[OrderBoardData]] = {
    val p: ResultSetParser[List[OrderBoardData]] = {
      (get[String]("user_id") ~ get[BigDecimal]("timestamp") ~ get[String]("side") ~ get[Int]("price") ~ get[BigDecimal]("size") ~ get[String]("orderId") map {
        case a ~ b ~ c ~ d ~ e ~ f => OrderBoardData(a, b, c, d, e, f)
      })*
    }
    getData[List[OrderBoardData]](s"select * from Trade_Order WHERE side = 'SELL' ORDER BY price ASC, timestamp ASC")(p)
  }
  def getMyOpenPosition(user_id: String, side: String): Option[List[OrderBoardData]] = {
    val p: ResultSetParser[List[OrderBoardData]] = {
      (get[String]("user_id") ~ get[BigDecimal]("timestamp") ~ get[String]("side") ~ get[Int]("price") ~ get[BigDecimal]("size") ~ get[String]("orderId") map {
        case a ~ b ~ c ~ d ~ e ~ f => OrderBoardData(a, b, c, d, e, f)
      })*
    }
    getData[List[OrderBoardData]](s"select * from Trade_Open_Position WHERE user_id = '${user_id}' AND side = '${side}' ORDER BY timestamp ASC")(p)
  }
  def insert(insertString: String): Either[String, Int] = {
    val r = db.withConnection { implicit connection =>
      SQL(insertString).executeUpdate()
    }
    if (r == 0) Left("insert failed") else Right(r)
  }
  def update(updateString: String): Either[String, Int] = {
    val r = db.withConnection { implicit connection =>
      SQL(updateString).executeUpdate()
    }
    if (r == 0) Left("insert failed") else Right(r)
  }
  def getData[T](query: String)(p: ResultSetParser[T]): Option[T] = {
    Option(db.withConnection { implicit connection =>
      SQL(query).as(p)
    })
  }
}
