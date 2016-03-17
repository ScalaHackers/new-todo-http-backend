package io.github.scalahackers.todo

/**
  * Created by love on 3/14/2016.
  */
import java.util.UUID

import akka.actor._

import scala.sys.process._

object TodoDataManager {

  def props(todoStorageActorRef: ActorRef): Props =
    Props(classOf[TodoDataManager], todoStorageActorRef)

  case class WorkComplete(result: Any)

}

// reference to master/TodoStorage
class TodoDataManager(todoStorageActorRef: ActorRef)
  extends Actor with ActorLogging {

  import JobProtocol._

  val workerId = UUID.randomUUID().toString

  // register
  todoStorageActorRef ! RegisterWorker(workerId, dataWorker)

  def receive = idle

  def idle: Receive = {
    case WorkIsReady =>
      // ack to master
      todoStorageActorRef ! JobProtocol.WorkIsReady

    //query database
    case todo: TodoTxs =>
      log.info("Got todo work: {}", todo.id)
      val currentWorkId = Some(todo.id)
      // do the work here
      val cmd = "ls -al"
      val output = Seq(cmd).!!
      println("Hello! " + output)
    // Process(cmd)
    //todoStorageActorRef ! new TodoResultUpdate(Option[output], Option[false], Option[0])
  }
}