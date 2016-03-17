package io.github.scalahackers.todo

import akka.http.scaladsl.marshallers.sprayjson._
import spray.json._

trait TodoMarshalling extends SprayJsonSupport
  with FlowMaterializerProvider
  with DefaultJsonProtocol {

  val standardTodoFormat = jsonFormat8(TodoTxs.apply)
  implicit val todoUpdateFormat = jsonFormat7(TodoUpdate.apply)

  def todoFormatFor(baseUrl: String) = new RootJsonFormat[TodoTxs] {
    def read(json: JsValue) = standardTodoFormat.read(json)

    def write(todo: TodoTxs) = {
      val fields = standardTodoFormat.write(todo).asJsObject.fields
      JsObject(fields.updated("url", JsString(baseUrl + '/' + todo.id)))
    }
  }
}
