package io.github.scalahackers.todo

import akka.actor._
import akka.http.scaladsl.Http
import akka.stream._

import scala.util._

object Main extends App
    with TodoStorage
    with TodoRoutes
    with TodoTable {
  val port = Properties.envOrElse("PORT", "8080").toInt
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

//  import driver.api._
//  val setupAction: DBIO[Unit] = DBIO.seq(todos.schema.create)
//  Await.result(db.run(setupAction), Duration.Inf)

  Http(system).bindAndHandle(routes, "0.0.0.0", port = port)
    .foreach(binding => system.log.info("Bound to " + binding.localAddress))

}
