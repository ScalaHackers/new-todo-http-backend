package io.github.scalahackers.todo

case class TodoUpdate(title: Option[String], completed: Option[Boolean], order: Option[Int])

case class TodoResultUpdate(title: Option[String], completed: Option[Boolean], order: Option[Int])
