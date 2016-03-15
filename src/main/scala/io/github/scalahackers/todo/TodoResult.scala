package io.github.scalahackers.todo

import scala.util.Random

case class TodoResult(id: String, title: String, completed: Boolean = false, order: Int = 0)

case object TodoResult {
  def create(title: String, resultUpdate: TodoResultUpdate): TodoResult = {
    TodoResult.create(TodoResult(nextId(), title), resultUpdate)
  }

  private def nextId() = Random.nextInt(Integer.MAX_VALUE).toString

  def create(old: TodoResult, resultUpdate: TodoResultUpdate): TodoResult =
    TodoResult(old.id,
      resultUpdate.title.getOrElse(old.title),
      resultUpdate.completed.getOrElse(old.completed),
      resultUpdate.order.getOrElse(old.order))
}
