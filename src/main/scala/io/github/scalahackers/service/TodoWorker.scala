package io.github.scalahackers.service

import java.util.UUID

//import scala.sys.process._
import scala.concurrent.duration._
import akka.actor._

object TodoWorker {

  def props(todoManagerActorRef: ActorRef): Props =
    Props(classOf[TodoWorker], todoManagerActorRef)

  case class WorkComplete(result: Any)

}

// reference to master/TodoStorage
//calls TodoWorker extends BaseWorker()
class TodoWorker(todoManagerActorRef: ActorRef)
  extends Actor with ActorLogging {

  import TodoWorker._
  import JobProtocol._
  import ManagerProtocol._

  val workerId = UUID.randomUUID().toString
  override def preStart(): Unit = {
    // register
    todoManagerActorRef ! RegisterWorker(workerId, todoState)
  }

  override def postStop(): Unit = {
    todoManagerActorRef ! UnRegisterWorker(workerId, todoState)
  }

  /* register
  override def postRestart(reason: Throwable): Unit = {
    todoStorageActorRef ! RegisterWorker(workerId)
  }*/

  def receive = idle

  def idle: Receive = {
    case todo: TodoTxs =>
      // done sth in state machine
      log.info("I am in worker: {} " , self.toString())
      log.info("Got todo work: {}", todo.id)
      Thread.sleep(5000)  //simulation
      // work on data validation, then change sub state and return to manager
      val currentWorkId = Some(todo.id)
      var output: String = "pass todo validation"
      try {
        //val cmd = "ls -la"
        //output = cmd.!!
        //output = WorkExecutor.callExtProgram(todo.request)
        log.info("Hello! " + output)
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
        todoManagerActorRef ! ManagerProtocol.WorkerResponse(
          workerId, todoState, todo, TodoUpdate(Option(todo.extid),
                        Option(todo.request),
                        Option(todoState),
                        Option(doneSubState),
                        Option(output.toString()), // response
                        None, None, None ))
      }
      //context.become(working)
      context.become(idle)
  }

  def working: Receive = {
    case WorkComplete(result) =>
      log.info("Work is complete. Result {}.", result)
      // done sth in state machine
      todoManagerActorRef ! ManagerProtocol.WorkerResponse
      context.become(WorkIsDoneAck(result))

    case _ =>
  }

  def WorkIsDoneAck(result: Any): Receive = {
    case WorkerAck(id) =>
      // done sth in state machine
      todoManagerActorRef ! ManagerProtocol.WorkerResponse
      context.become(idle)
  }
}