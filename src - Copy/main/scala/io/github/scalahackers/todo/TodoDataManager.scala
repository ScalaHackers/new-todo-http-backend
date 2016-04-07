package io.github.scalahackers.todo

import java.util.UUID

import scala.sys.process._
import scala.concurrent.duration._
import akka.actor._
import akka.actor.{ Props, Deploy, Address, AddressFromURIString }

object RemoteActor {

  def props(todoManagerActorRef: ActorRef): Props =
    Props(classOf[RemoteActor], todoManagerActorRef)
}

// reference to master/TodoStorage
class RemoteActor(todoManagerActorRef: ActorRef)
  extends Actor with ActorLogging {

  import JobProtocol._

  val workerId = UUID.randomUUID().toString
  override def preStart(): Unit = {
    // register
    todoManagerActorRef ! RegisterWorker(workerId, remoteState)
  }

  override def postStop(): Unit = {
    todoManagerActorRef ! UnRegisterWorker(workerId, remoteState)
  }

  def receive = idle

  //import context.dispatcher
  //val registerTask = context.system.scheduler.schedule(0.seconds, 10.seconds, todoStorageActorRef,
  //  RegisterWorker(workerId))

  def idle: Receive = {
    case JobProtocol.WorkIsReady =>
      // ack to master
      todoManagerActorRef ! JobProtocol.WorkIsReady

    case todo: TodoTxs =>
      log.info("Remote: got todo work: {}", todo.id)
      // work on data validation, then change state and return to manager
      val currentWorkId = Some(todo.id)
      var output: String = "test"
      // do the work here
      try {
        //val cmd = "dir"
        //output = Seq(cmd).!!
        val cmd = "ls -al" + todo.id
        Thread.sleep(5000)
        log.debug("Hello! " + output)
      }
      catch {
        case ex: Exception => log.error("Todo error: {}" + ex, todo.id)
      }
      finally {
        // Process(cmd)
        todoManagerActorRef ! ManagerProtocol.WorkerResponse(
          workerId, remoteState, todo, TodoUpdate(Option(todo.extid),
            Option(todo.request),
            Option(remoteState),
            Option(doneSubState),
            Option(output.toString()), // response
            None, None, None ))

      }
  }
}