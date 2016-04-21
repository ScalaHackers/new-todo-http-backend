package io.github.scalahackers.service

import java.util.UUID

import scala.sys.process._
import scala.concurrent.duration._
import akka.actor._

object SearchWorker {

  def props(todoManagerActorRef: ActorRef): Props =
    Props(classOf[TodoWorker], todoManagerActorRef)

  //case class WorkComplete(result: Any)

}

// reference to master/TodoStorage
class SearchWorker(todoManagerActorRef: ActorRef)
  extends Actor with ActorLogging {

  import JobProtocol._

  val workerId = UUID.randomUUID().toString
  override def preStart(): Unit = {
    // register
    todoManagerActorRef ! RegisterWorker(workerId, searchState)
  }

  override def postStop(): Unit = {
    todoManagerActorRef ! UnRegisterWorker(workerId, searchState)
  }

  /* register
  override def postRestart(reason: Throwable): Unit = {
    todoStorageActorRef ! RegisterWorker(workerId)
  }*/

  def receive = idle

  //import context.dispatcher
  //val registerTask = context.system.scheduler.schedule(0.seconds, 10.seconds, todoStorageActorRef,
  //  RegisterWorker(workerId))

  def idle: Receive = {
    case WorkIsReady =>
      // ack to master
      todoManagerActorRef ! JobProtocol.WorkIsReady

    case todo: TodoTxs =>
      log.info("Got todo work: {}", todo.id)
      // work on data validation, then change state and return to manager
      val currentWorkId = Some(todo.id)
      var output: String = "test"
      // do the work here
      try {
        //val cmd = "dir"
        //output = Seq(cmd).!!
        log.debug("Hello! " + output)
      }
      catch {
        case ex: Exception => log.error("Todo error: {}" + ex, todo.id)
      }
      finally {
        // Process(cmd)
        //todoStorageActorRef ! new TodoResultUpdate(Option[output], Option[false], Option[0])
        //todoStorageActorRef ! JobProtocol.WorkIsDone
        todoManagerActorRef ! ManagerProtocol.WorkerResponse(
          workerId, searchState, todo, TodoUpdate(Option(todo.extid),
            Option(todo.request),
            Option(searchState),
            Option(doneSubState),
            Option(output.toString()), // response
            None, None, None))

      }
  }
}