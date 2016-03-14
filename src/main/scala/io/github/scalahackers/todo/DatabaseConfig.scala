package io.github.scalahackers.todo

trait DatabaseConfig {
  val driver = slick.driver.MySQLDriver

  import driver.api._

  def db = Database.forConfig("mysqldb")

  implicit val session: Session = db.createSession()
}
