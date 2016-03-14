package io.github.scalahackers.todo

import java.util.UUID
import com.datainc.pipeline.workflow.MasterWorkerProtocol.RegisterWorker
import com.datainc.pipeline.workflow.{MasterWorkerProtocol, Work}
import com.datainc.pipeline.workflow.MasterWorkerProtocol.Ack
import com.datainc.pipeline.workflow.MasterWorkerProtocol.RegisterWorker
import com.datainc.pipeline.workflow.MasterWorkerProtocol.WorkFailed
import com.datainc.pipeline.workflow.MasterWorkerProtocol.WorkIsDone
import com.datainc.pipeline.workflow.MasterWorkerProtocol.WorkIsReady
import com.datainc.pipeline.workflow.MasterWorkerProtocol.WorkerRequestsWork
import com.datainc.pipeline.workflow.TodoWorker.WorkComplete
import com.datainc.pipeline.workflow.TodoWorker.WorkComplete

import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ReceiveTimeout
import akka.actor.Terminated
import akka.cluster.client.ClusterClient.SendToAll
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy.Stop
import akka.actor.SupervisorStrategy.Restart
import akka.actor.ActorInitializationException
import akka.actor.DeathPactException

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

    // register
  todoStorageActorRef ! RegisterWorker(workerId)

  def receive = idle

  def idle: Receive = {
    case WorkIsReady =>
      // ack to master
      todoStorageActorRef ! todo.id

    case todo: Todo =>
      log.info("Got todo work: {}", job)
      val currentWorkId = Some(todo.id)
    // do the work here
    println("do the work")
      todoStorageActorRef ! TodoResult
  }
}