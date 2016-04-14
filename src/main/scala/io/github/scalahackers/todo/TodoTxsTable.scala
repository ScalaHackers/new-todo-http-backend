package io.github.scalahackers.todo

//trait TodoTxsTable extends DatabaseConfig {
trait TodoTxsTable extends DatabaseConfigOracle {

  import driver.api._

  // same table
  protected val todos = TableQuery[TodosTxs]

  class TodosTxs(tag: Tag) extends Table[TodoTxs](tag, "TXS_SM") {

    def id = column[String]("ID", O.PrimaryKey)

    def extid = column[String]("EXTID")

    //def request = column[String]("REQUEST")
    def reqtype = column[String]("REQTYPE")

    def reqtask = column[String]("REQTASK")

    def state = column[String]("STATE")

    def substate = column[String]("SUBSTATE")

    def response = column[String]("RESPONSE")

    def priority = column[Int]("PRIORITY")

    def starttime = column[String]("STARTTIME")

    def endtime = column[String]("ENDTIME")

    private type RequestPayloadTupleType = (String, String)
    //private val RequestPayloadShapedValue = (reqtype, reqtask).shaped[RequestPayloadTupleType]

    private type TodoTxsTupleType = (String, String, RequestPayloadTupleType, String, String, String, Int, String, String)
    private val TodoTxsShapedValue = (id, extid, (reqtype, reqtask), state, substate, response, priority, starttime, endtime
      ).shaped[TodoTxsTupleType]

    private val toModel: TodoTxsTupleType => TodoTxs = { todoTuple =>
      TodoTxs(
        id = todoTuple._1,
        extid = todoTuple._2,
        request = RequestPayload.tupled.apply(todoTuple._3),
        state = todoTuple._4,
        substate = todoTuple._5,
        response = todoTuple._6,
        priority = todoTuple._7,
        starttime = todoTuple._8,
        endtime = todoTuple._9
      )
    }
    private val toTuple: TodoTxs => Option[TodoTxsTupleType] = { todo =>
      Some {
        (todo.id,
          todo.extid,
          (RequestPayload.unapply(todo.request).get),
          todo.state,
          todo.substate,
          todo.response,
          todo.priority,
          todo.starttime,
          todo.endtime)

      }
    }

    def * = TodoTxsShapedValue <> (toModel, toTuple)
    //def * = (id, extid, (reqtype, reqtask), state, substate, response, priority, starttime, endtime)
      //<>((TodoTxs.apply _).tupled, TodoTxs.unapply)
  }
}
