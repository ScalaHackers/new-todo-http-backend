package io.github.scalahackers.todo

trait TodoTable extends DatabaseConfig {

  import driver.api._

  class Todos(tag: Tag) extends Table[Todo](tag, "todos") {
    def id = column[String]("id", O.PrimaryKey)
    def title = column[String]("title")
    def completed = column[Boolean]("completed")
    def order = column[Int]("order")

    def * = (id, title, completed, order) <> ((Todo.apply _).tupled, Todo.unapply)
  }

  protected val todos = TableQuery[Todos]
}
