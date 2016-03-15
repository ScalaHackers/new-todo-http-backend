package io.github.scalahackers.todo

trait TodoTable extends DatabaseConfig {

  import driver.api._

  // same table
  protected val todos = TableQuery[Todos]
  protected val todoResults = TableQuery[Todos]

  class Todos(tag: Tag) extends Table[Todo](tag, "todos") {
    def * = (id, title, completed, order) <>((Todo.apply _).tupled, Todo.unapply)

    def id = column[String]("id", O.PrimaryKey)

    //def extid = column[String]("extid")

    def title = column[String]("title")

    def completed = column[Boolean]("completed")

    def order = column[Int]("order")

    // def result = column[String]("result")
  }

  class TodoResults(tag: Tag) extends Table[TodoResult](tag, "todos") {
    def * = (id, title, completed, order) <>((TodoResult.apply _).tupled, TodoResult.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def title = column[String]("title")

    def completed = column[Boolean]("completed")

    def order = column[Int]("order")
  }

}
