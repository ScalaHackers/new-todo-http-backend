package io.github.scalahackers.todo

case class TodoUpdate(extid: Option[String],
                      request: Option[String],
                      state: Option[String],
                      substate: Option[String],
                      response: Option[String],
                      starttime: Option[String],
                      endtime: Option[String])

case class TodoInsert(id: String,
                      extid: Option[String],
                      request: String,
                      state: Option[String],
                      substate: Option[String],
                      response: Option[String],
                      starttime: Option[String],
                      endtime: Option[String])