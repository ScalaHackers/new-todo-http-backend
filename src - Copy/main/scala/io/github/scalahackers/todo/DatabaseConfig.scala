package io.github.scalahackers.todo

trait DatabaseConfig {
  val driver = slick.driver.MySQLDriver

  import driver.api._
  implicit val session: Session = db.createSession()

  def db = Database.forConfig("mysqldb")
}
