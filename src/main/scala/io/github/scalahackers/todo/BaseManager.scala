package io.github.scalahackers.todo

import akka.actor._

// worker state table
sealed trait WorkerBaseState

class BaseManager extends Actor with TodoTxsTable with ActorLogging {

  // workers state is not event sourced
  private var clients = Map[String, ActorRef]()

  // workers state is not event sourced
  private var workers = Map[String, WorkerBaseState]()

  def receive = {
    case JobProtocol.RegisterWorker(workerId, workerType) =>
      sender() ! JobProtocol.WorkIsReady
  }
}
