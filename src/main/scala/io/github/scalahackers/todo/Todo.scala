package io.github.scalahackers.todo

import scala.util.Random

case class Todo(id: String, title: String, completed: Boolean = false, order: Int = 0)
//case class Todo(txsId: String, extId: String, url: String, state: Int = 0, order: Int = 0, result: String)

case object Todo {
  def create(title: String, todoUpdate: TodoUpdate): Todo = {
    Todo.create(Todo(nextId(), title), todoUpdate)
  }

  private def nextId() = Random.nextInt(Integer.MAX_VALUE).toString

  def create(old: Todo, update: TodoUpdate): Todo =
    Todo(old.id,
      update.title.getOrElse(old.title),
      update.completed.getOrElse(old.completed),
      update.order.getOrElse(old.order))
}
