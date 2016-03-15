package io.github.scalahackers.todo

case class TodoUpdate(extid: Option[String], title: Option[String], state: Option[Int], order: Option[Int], result: Option[String])

