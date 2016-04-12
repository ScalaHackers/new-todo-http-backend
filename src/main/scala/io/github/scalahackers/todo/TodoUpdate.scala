package io.github.scalahackers.todo

case class TodoUpdate(extid: Option[String],
                      request: Option[RequestPayload],
                      state: Option[String],
                      substate: Option[String],
                      response: Option[String],
                      priority: Option[Int],
                      starttime: Option[String],
                      endtime: Option[String])

case class TodoInsert(id: String,
                      extid: Option[String],
                      request: RequestPayload,
                      state: Option[String],
                      substate: Option[String],
                      response: Option[String],
                      priority: Option[Int],
                      starttime: Option[String],
                      endtime: Option[String])