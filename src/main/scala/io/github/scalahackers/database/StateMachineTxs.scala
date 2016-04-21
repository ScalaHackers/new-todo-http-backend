package io.github.scalahackers.database

import io.github.scalahackers.service.RequestPayload
import io.github.scalahackers.service.TodoTxs

//import slick.profile.RelationalTableComponent.Table

//trait SmTxsTable extends DatabaseConfig {
trait TodoTxsTable extends DatabaseConfigOracle {

  import driver.api._
  import java.sql.Timestamp
  import slick.lifted.{Shape, ShapeLevel, TupleShape}
  import slick.util.TupleSupport
  import slick.lifted.Rep

  case class LiftedRequestPayload(reqtype: Rep[String],
                                  reqtask: Rep[String])

  case class LiftedTodoTxs(id: Rep[String],
                           extid: Rep[String],
                           request: LiftedRequestPayload,
                           state: Rep[String],
                           substate: Rep[String],
                           response: Rep[String],
                           priority: Rep[Int],
                           starttime: Rep[String],
                           endtime: Rep[String])

  implicit object RequestPayloadShape extends CaseClassShape(LiftedRequestPayload.tupled, RequestPayload.tupled)

  class RequestPayloadRow(tag: Tag) extends Table[RequestPayload](tag, "REQUEST_PAYLOAD") {
    def reqtype = column[String]("REQTYPE")
    def reqtask = column[String]("REQTASK")
    def * = LiftedRequestPayload(reqtype, reqtask)
  }
  val requestpayload = TableQuery[RequestPayloadRow]


  implicit object TodoTxsShape extends CaseClassShape(LiftedTodoTxs.tupled, (TodoTxs.apply _).tupled)

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

    def projection = LiftedTodoTxs(
      column[String]("ID"),
      column[String]("EXTID"),
      LiftedRequestPayload(reqtype, reqtask),
      column[String]("STATE"),
      column[String]("SUBSTATE"),
      column[String]("RESPONSE"),
      column[Int]("PRIORITY"),
      column[String]("STARTTIME"),
      column[String]("ENDTIME")
      // (cols defined inline, type inferred)
    )

    def * = projection
//    def * = (id.?, extid, request, state, substate, response, priority, starttime, endtime) <>
//      ((TodoTxs.apply _).tupled, TodoTxs.unapply)

//      { mappedRequest => RequestPayload(reqtype, reqtask)
//      },
//      { mappedResult => RequestPayload(reqtype, reqtask)
//      })

    //def * = (id, extid, (reqtype, reqtask), state, substate, response, priority, starttime, endtime)
    //<>((TodoTxs.apply _).tupled, TodoTxs.unapply)
  }

  protected val todos = TableQuery[TodosTxs]

}
