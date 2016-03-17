package io.github.scalahackers.todo

import java.util.UUID

import scala.sys.process._
import scala.concurrent.duration._
import akka.actor._

object TodoWorker {

  def props(todoStorageActorRef: ActorRef): Props =
    Props(classOf[TodoWorker], todoStorageActorRef)

  case class WorkComplete(result: Any)

}

// reference to master/TodoStorage
class TodoWorker(todoStorageActorRef: ActorRef)
  extends Actor with ActorLogging {

  import JobProtocol._

  val workerId = UUID.randomUUID().toString
  override def preStart(): Unit = {
    // register
    todoStorageActorRef ! RegisterWorker(workerId, todoWorker)
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
      todoStorageActorRef ! JobProtocol.WorkIsReady

    case todo: Todo =>
      println("Got todo work")
      log.info("Got todo work: {}", todo.id)
      // work on data validation, then change sub state and return to manager
      val currentWorkId = Some(todo.id)
      var output: String = "pass todo validation"
      try {
        //val cmd = "data validation"
        //output = Seq(cmd).!!
        println("Hello! " + output)
      }
      catch {
        case ex: Exception => log.error("Todo error: {}" + ex, todo.id)
      }
      finally {
        // Process(cmd)
        //todoStorageActorRef ! new TodoResultUpdate(Option[output], Option[false], Option[0])
        //todoStorageActorRef ! JobProtocol.WorkIsDone
        todoStorageActorRef ! TodoStorageActor.Response(
            TodoUpdate(Option(todo.id), Option(output.toString()),
              Option(JobProtocol.validateState), Option(0), Option(output.toString())))
      }
  }
}