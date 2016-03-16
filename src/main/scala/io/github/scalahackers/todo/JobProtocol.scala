package io.github.scalahackers.todo

object JobProtocol {

  // Messages from clients/external to manager
  case class RegisterClient(clientName: String)

  case class NotifyClient(clientName: String, result: Any)

  // Messages from Workers to manager
  case class RegisterWorker(workerId: String)

  case class WorkerRequestsWork(workerId: String)

  case object WorkIsReady

  case class WorkIsDone(workerId: String, workId: String, result: Any)

  case class WorkFailed(workerId: String, workId: String)

  case class Ack(id: String)

  // Messages to Workers

  // state machine protocol
  val initStat    = 0
  val prepStat    = 1
  val validateStat= 2
  val finalStat   = 10
}