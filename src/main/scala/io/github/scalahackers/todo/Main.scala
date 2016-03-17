package io.github.scalahackers.todo

import akka.actor._
import akka.http.scaladsl.Http
import akka.stream._

import scala.util._

object Main extends App
  //with TodoManager
  with TodoRoutes
  with TodoTxsTable {

  // get it from config factory, TBD
  val httpHost = "0.0.0.0"
  // val httpHost = Properties.envOrElse("HOST", "0.0.0.0").toString
  val port = Properties.envOrElse("PORT", "8080").toInt
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val todoManager: ActorRef = system.actorOf(Props(new TodoManagerActor))

  //  import driver.api._
  //  val setupAction: DBIO[Unit] = DBIO.seq(todos.schema.create)
  //  Await.result(db.run(setupAction), Duration.Inf)

  Http(system).bindAndHandle(routes, httpHost, port = port)
    .foreach(binding => system.log.info("Bound to " + binding.localAddress))

  todoManager ! JobProtocol.Ack("started!")
}
