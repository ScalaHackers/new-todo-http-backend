

object CodeGen2 extends App {
  //https://stackoverflow.com/questions/28285129/slick-code-generation-for-only-a-single-schema

  val slickDriver = "com.typesafe.slick.driver.oracle.OracleDriver"
  val jdbcDriver = "oracle.jdbc.OracleDriver"
  val url = "jdbc:oracle:thin:@//localhost:1521/XE" //jdbc:oracle:thin:@localhost:1521:XE"
  val user = "dbuser"
  val password = "testtest"
  val destDir = "src/main/scala"
  val destPackage = "io.github.scalahackers.todo"

  import scala.concurrent.{ExecutionContext, Await, Future}
  import scala.concurrent.duration.Duration
  import slick.codegen.SourceCodeGenerator
  import scala.concurrent.ExecutionContext.Implicits.global
  import slick.jdbc.JdbcModelBuilder
  import slick.jdbc.meta.MTable
  import com.typesafe.slick.driver.oracle.OracleDriver
  import slick.jdbc.JdbcBackend.DatabaseFactoryDef

  println("Starting codegen...")
  val db = OracleDriver.simple.Database.forURL(url, user=user, password=password, driver=jdbcDriver)
  val filteredTables = OracleDriver.defaultTables.filter(
    (t: MTable) => !t.name.schema.get.endsWith("STAGE")
  )

  val modelAction = OracleDriver.createModel(filteredTables, true)
  println("Generating model...")
  val model = Await.result(db.run(modelAction), Duration.Inf)
  val codegen = new SourceCodeGenerator(model) {
    // for illustration
    val noStage = model.tables.filter { table => !table.name.schema.get.endsWith("STAGE") }
    noStage.foreach { table => println(table.name.schema.get) }

  }
  println("Generating files...")
  codegen.writeToFile(
    slickDriver, destDir, destPackage, "Tables", "Tables.scala"
  )
  //  slick.codegen.SourceCodeGenerator.main(
  //    Array(slickDriver, jdbcDriver, url, destDir, destPackage, user, password)
  //  )
  println("Finished codegen.")
}