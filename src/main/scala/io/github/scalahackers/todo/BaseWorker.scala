package io.github.scalahackers.todo

import java.util.UUID

import akka.actor._

// reference to master/TodoStorage
class BaseWorker(managerRef: ActorRef)
  extends Actor with ActorLogging {

  import JobProtocol._

  val workerId = UUID.randomUUID().toString
  override def preStart(): Unit = {
    // register
    managerRef ! RegisterWorker(workerId, todoWorker)
  }

  def receive = idle

  def idle: Receive = {
    case WorkIsReady =>
      managerRef ! JobProtocol.WorkIsReady

    case task: TodoTxs =>
      managerRef ! JobProtocol.WorkIsDone
  }
}