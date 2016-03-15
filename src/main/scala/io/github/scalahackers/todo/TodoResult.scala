package io.github.scalahackers.todo

import scala.util.Random

// map to the same table
//case class TodoResult(id: String, title: String, completed: Boolean = false, order: Int = 0)
case class TodoResult(id: String, extid: String = "", title: String, state: Int = 0, order: Int = 0, result: String = "")


case object TodoResult {
  def create(title: String, resultUpdate: TodoUpdate): TodoResult = {
    TodoResult.create(TodoResult(nextId(), "", title), resultUpdate)
  }

  private def nextId() = Random.nextInt(Integer.MAX_VALUE).toString

  def create(old: TodoResult, resultUpdate: TodoUpdate): TodoResult =
    TodoResult(old.id,
      resultUpdate.extid.getOrElse(old.extid),
      resultUpdate.title.getOrElse(old.title),
      resultUpdate.state.getOrElse(old.state),
      resultUpdate.order.getOrElse(old.order),
      resultUpdate.result.getOrElse(old.result))
}

