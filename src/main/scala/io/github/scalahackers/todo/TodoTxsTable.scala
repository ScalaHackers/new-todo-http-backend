package io.github.scalahackers.todo

trait TodoTxsTable extends DatabaseConfig {
//trait TodoTxsTable extends DatabaseConfigOracle {

  import driver.api._

  // same table
  protected val todos = TableQuery[TodosTxs]

  class TodosTxs(tag: Tag) extends Table[TodoTxs](tag, "TXS_SM") {
    def * = (id, extid, request, state, substate, response, priority, starttime, endtime) <>((TodoTxs.apply _).tupled, TodoTxs.unapply)

    def id = column[String]("ID", O.PrimaryKey)

    def extid = column[String]("EXTID")

    def request = column[String]("REQUEST")

    def state = column[String]("STATE")

    def substate = column[String]("SUBSTATE")

    def response = column[String]("RESPONSE")

    def priority = column[Int]("PRIORITY")

    def starttime = column[String]("STARTTIME")

    def endtime = column[String]("ENDTIME")
  }

}
