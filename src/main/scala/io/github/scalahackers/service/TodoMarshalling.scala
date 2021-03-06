package io.github.scalahackers.service

import akka.http.scaladsl.marshallers.sprayjson._
import spray.json._

trait TodoMarshalling extends SprayJsonSupport
  with FlowMaterializerProvider
  with DefaultJsonProtocol {

  implicit val standardRequestPayloadFormat  = jsonFormat2(RequestPayload.apply)
  val standardTodoFormat  = jsonFormat9(TodoTxs.apply)
  implicit val todoUpdateFormat = jsonFormat8(TodoUpdate.apply)

  def todoFormatFor(baseUrl: String) = new RootJsonFormat[TodoTxs] {
    def read(json: JsValue) = standardTodoFormat.read(json)

    def write(todo: TodoTxs) = {
      val fields = standardTodoFormat.write(todo).asJsObject.fields
      JsObject(fields.updated("url", JsString(baseUrl + '/' + todo.id)))
    }
  }
}
