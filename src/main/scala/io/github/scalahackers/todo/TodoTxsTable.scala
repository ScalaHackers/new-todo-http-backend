package io.github.scalahackers.todo

//trait TodoTxsTable extends DatabaseConfig {
trait TodoTxsTable extends DatabaseConfigOracle {

  import driver.api._

  // same table
  protected val todos = TableQuery[TodosTxs]
  protected val todoResults = TableQuery[TodosTxs]

  class TodosTxs(tag: Tag) extends Table[TodoTxs](tag, "txs_sm") {
    def * = (id, extid, request, state, substate, response, starttime, endtime) <>((TodoTxs.apply _).tupled, TodoTxs.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def extid = column[String]("extid")

    def request = column[String]("request")

    def state = column[String]("state")

    def substate = column[String]("substate")

    def response = column[String]("response")

    def starttime = column[String]("starttime")

    def endtime = column[String]("endtime")
  }

}
