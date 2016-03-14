package io.github.scalahackers.todo

import java.util.UUID
import akka.actor._
import scala.sys.process._

object TodoWorker {

  def props(todoStorageActorRef: ActorRef): Props =
    Props(classOf[TodoWorker], todoStorageActorRef)

  case class WorkComplete(result: Any)
}

  // reference to master/TodoStorage
class TodoWorker(todoStorageActorRef: ActorRef)
  extends Actor with ActorLogging {
  import JobProtocol._
  import TodoStorageActor._

  val workerId = UUID.randomUUID().toString

    // register
  todoStorageActorRef ! RegisterWorker(workerId)

  def receive = idle

  def idle: Receive = {
    case WorkIsReady =>
      // ack to master
      todoStorageActorRef ! JobProtocol.WorkIsReady

    case todo: Todo =>
      log.info("Got todo work: {}", todo.id)
      val currentWorkId = Some(todo.id)
      // do the work here
      val cmd = "ls -al"
      val output =  cmd.!!
      //todoStorageActorRef ! new TodoResultUpdate(Option[output], false, 0)
  }
}