package io.github.scalahackers.todo

import akka.http.scaladsl.marshallers.sprayjson._
import spray.json._

trait TodoMarshalling extends SprayJsonSupport
  with FlowMaterializerProvider
  with DefaultJsonProtocol {

  implicit val standardTodoFormat = jsonFormat9(TodoTxs.apply)
  implicit val todoUpdateFormat = jsonFormat8(TodoUpdate.apply)
  //val standardTodoFormat = jsonFormat(TodoTxs("ID", "EXTID", "REQUEST", "STATE", "SUBSTATE", "RESPONSE", "PRIORITY", "STARTTIME", "ENDTIME"))

  def todoFormatFor(baseUrl: String) = new RootJsonFormat[TodoTxs] {
    def read(json: JsValue) = standardTodoFormat.read(json)

    def write(todo: TodoTxs) = {
      val fields = standardTodoFormat.write(todo).asJsObject.fields
      JsObject(fields.updated("url", JsString(baseUrl + '/' + todo.id)))
    }
  }
}
