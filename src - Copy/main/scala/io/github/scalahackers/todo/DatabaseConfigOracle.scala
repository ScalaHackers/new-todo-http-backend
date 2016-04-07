package io.github.scalahackers.todo

import com.typesafe.slick.driver.oracle.OracleDriver

/**
  * Created by hdong on 3/14/2016.
  */
trait DatabaseConfigOracle {
  val driver = com.typesafe.slick.driver.oracle.OracleDriver

  import driver.api._
  implicit val session: Session = db.createSession()

  def db = Database.forConfig("oracledb")
}
