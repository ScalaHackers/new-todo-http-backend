package io.github.scalahackers.service

import akka.actor._
import akka.http.scaladsl.Http
import akka.stream._
import com.typesafe.config.ConfigFactory


import scala.util._

object Main extends App
  //with TodoManager
  //with Service
  with TodoRoutes
  with TodoTxsTable {

  // get it from config factory, TBD
  val httpHost = Properties.envOrElse("HOST", "0.0.0.0").toString
  val httpPort = Properties.envOrElse("PORT", "8088").toInt
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  //override val logger = Logging(system, getClass)

  // load todo.conf
  //val conf = ConfigFactory.parseString("akka.remote.http.port=" + port).
  //  withFallback(ConfigFactory.load("todo"))


  val todoManager: ActorRef = system.actorOf(Props(new TodoManagerActor))

  //  import driver.api._
  //  val setupAction: DBIO[Unit] = DBIO.seq(todos.schema.create)
  //  Await.result(db.run(setupAction), Duration.Inf)

  Http(system).bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
    .foreach(binding => system.log.info("Bound to " + binding.localAddress))

  todoManager ! JobProtocol.WorkerAck("started!")
}
