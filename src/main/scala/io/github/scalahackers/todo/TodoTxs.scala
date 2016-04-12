package io.github.scalahackers.todo

import scala.util.Random

case class TodoTxs(id: String, extid: String = "", request: RequestPayload, state: String = "", substate: String = "",
                   response: String = "", priority: Int = 0, starttime: String = "", endtime: String = "")

case class RequestPayload(reqtype: String, reqtask: String)

case object RequestPayload extends RequestPayload

case object TodoTxs {
  def create(request: RequestPayload, todoUpdate: TodoUpdate): TodoTxs = {
    TodoTxs.create(TodoTxs(nextId(), "", request, JobProtocol.initState), todoUpdate)
  }

  private def nextId() = Random.nextInt(Integer.MAX_VALUE).toString

  //val today = Calendar.getInstance().getTime()

  def create(old: TodoTxs, update: TodoUpdate): TodoTxs =
    TodoTxs(old.id,
      update.extid.getOrElse(old.extid),
      update.request.getOrElse(old.request),
      update.state.getOrElse(old.state),
      update.substate.getOrElse(old.substate),
      update.response.getOrElse(old.response),
      update.priority.getOrElse(old.priority),
      update.starttime.getOrElse(old.starttime),
      update.endtime.getOrElse(old.endtime))
}
