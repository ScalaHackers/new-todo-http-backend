package io.github.scalahackers.todo

trait TodoTable extends DatabaseConfig {

  import driver.api._

  protected val todos = TableQuery[Todos]
  protected val todoResults = TableQuery[TodoResults]

  class Todos(tag: Tag) extends Table[Todo](tag, "todos") {
    def * = (id, title, completed, order) <>((Todo.apply _).tupled, Todo.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def title = column[String]("title")

    def completed = column[Boolean]("completed")

    def order = column[Int]("order")
  }

  class TodoResults(tag: Tag) extends Table[TodoResult](tag, "todoResults") {
    def * = (id, title, completed, order) <>((TodoResult.apply _).tupled, TodoResult.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def title = column[String]("title")

    def completed = column[Boolean]("completed")

    def order = column[Int]("order")
  }

}
