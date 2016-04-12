package io.github.scalahackers.todo

import akka.http.scaladsl.marshallers.sprayjson._
import oracle.net.aso.s
import spray.json._
import spray.json.DefaultJsonProtocol._

trait NestedMashalling extends SprayJsonSupport
  with FlowMaterializerProvider
  with DefaultJsonProtocol {

  implicit val nestedRequestFormatFor = jsonFormat(RequestPayload, "reqtype", "reqtask")
  implicit val nestedTodoFormatFor = jsonFormat(TodoTxs,
    "id", "extid", "request", "state", "substate", "response", "priority", "starttime", "endtime")
//  implicit val nestedTodoRequestFormatFor extends JsonFormat[TodoTxs] {
//    def read(json: JsValue) {
//        json.asJsObject.getFields("reqtype", "reqtask") match {
//      case Seq(JsString(reqtype), JsString(reqtask)) => RequestPayload(reqtype, reqtask)
//      case _ => throw new DeserializationException("RequestPayload is expected")
//    }
//
//    def write(payload: RequestPayload) = JsString(payload.reqtype, payload.reqtask)
//  }
//  implicit val nestedTodoFormatFor = jsonFormat9(TodoTxs.apply)

  // TBD: the same thing for TodoUpdate later
}
