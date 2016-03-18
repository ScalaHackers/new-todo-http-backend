package io.github.scalahackers.todo

import java.util.UUID

import scala.sys.process._
import scala.concurrent.duration._
import akka.actor._

object TodoWorker {

  def props(todoManagerActorRef: ActorRef): Props =
    Props(classOf[TodoWorker], todoManagerActorRef)

  case class WorkComplete(result: Any)

}

// reference to master/TodoStorage
class TodoWorker(todoManagerActorRef: ActorRef)
  extends Actor with ActorLogging {

  import JobProtocol._

  val workerId = UUID.randomUUID().toString
  override def preStart(): Unit = {
    // register
    todoManagerActorRef ! RegisterWorker(workerId, todoWorker)
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
      println("Got todo work: {}", todo.id)
      log.info("Got todo work: {}", todo.id)
      // work on data validation, then change sub state and return to manager
      val currentWorkId = Some(todo.id)
      var output: String = "pass todo validation"
      try {
        //val cmd = "cmd dir "
        //output = cmd.!!
        println("Hello! " + output)
      }
      catch {
        case ex: Exception => {
          log.error("Todo error: {} {}", ex, todo.id)
        }
      }
      finally {
        // Process(cmd)
        //todoStorageActorRef ! new TodoResultUpdate(Option[output], Option[false], Option[0])
        //todoStorageActorRef ! JobProtocol.WorkIsDone
        todoManagerActorRef ! TodoManagerActor.Response(
/*            TodoUpdate(Option(todo.extid),
              Option(todo.request),
              Option(JobProtocol.validateState),
              Option(JobProtocol.doneSubState),
              Option(output.toString()), // response
              None, None  // startime and endtime, TBD */
              todo, TodoUpdate(Option(todo.extid),
                        Option(todo.request),
                        Option(JobProtocol.validateState),
                        Option(JobProtocol.doneSubState),
                        Option(output.toString()), // response
                        None, None ))
      }
  }
}