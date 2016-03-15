package io.github.scalahackers.todo

import scala.util.Random

//case class Todo(id: String, title: String, completed: Boolean = false, order: Int = 0)
case class Todo(id: String, extid: String = "", title: String, state: Int = 0, order: Int = 0, result: String = "")

case object Todo {
  def create(title: String, todoUpdate: TodoUpdate): Todo = {
    Todo.create(Todo(nextId(), "", title), todoUpdate)
  }

  private def nextId() = Random.nextInt(Integer.MAX_VALUE).toString

  def create(old: Todo, update: TodoUpdate): Todo =
    Todo(old.id,
      update.extid.getOrElse(old.extid),
      update.title.getOrElse(old.title),
      update.state.getOrElse(old.state),
      update.order.getOrElse(old.order),
      update.result.getOrElse(old.result))
}
