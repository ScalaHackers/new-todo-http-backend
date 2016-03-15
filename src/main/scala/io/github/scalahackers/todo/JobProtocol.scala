package io.github.scalahackers.todo

object JobProtocol {

  // Messages from Workers to manager
  case class RegisterWorker(workerId: String)

  case class WorkerRequestsWork(workerId: String)

  case class WorkIsDone(workerId: String, workId: String, result: Any)

  case class WorkFailed(workerId: String, workId: String)

  case class Ack(id: String)

  // Messages to Workers
  case object WorkIsReady

  // state machine protocol
  val initStat    = 0
  val prepStat    = 1
  val validateStat= 2
  val finalStat   = 10
}