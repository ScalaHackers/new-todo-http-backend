package io.github.scalahackers.todo

import akka.http.scaladsl.marshallers.sprayjson._
import oracle.net.aso.s
import spray.json._
import spray.json.DefaultJsonProtocol._

trait NestedMashalling extends SprayJsonSupport
  with FlowMaterializerProvider
  with DefaultJsonProtocol {

  implicit val nestedRequestFormatFor = jsonFormat2(RequestPayload.apply)
  implicit val nestedTodoFormatFor = jsonFormat9(TodoTxs.apply)
//  implicit val nestedTodoRequestFormatFor extends JsonFormat[TodoTxs] {
//    def read(json: JsValue) {
//        json.asJsObject.getFields("reqtype", "reqtask") match {
//      case Seq(JsString(reqtype), JsString(reqtask)) => RequestPayload(reqtype, reqtask)
//      case _ => throw new DeserializationException("RequestPayload is expected")
//    }
//
//    def write(payload: RequestPayload) = JsString(payload.reqtype, payload.reqtask)
//  }

  // TBD: the same thing for TodoUpdate later
}
