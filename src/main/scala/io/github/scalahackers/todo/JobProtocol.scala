package io.github.scalahackers.todo

object JobProtocol {

  // Messages from clients/external to manager
  case class RegisterClient(clientName: String, clientType: Int)

  case class NotifyClient(clientName: String, result: Any)

  // Messages from Workers to manager
  case class RegisterWorker(workerId: String, workerType: String)

  case class WorkerRequestsWork(workerId: String)

  case object WorkIsReady

  case class WorkIsDone(workerId: String, workId: String, result: Any)

  case class WorkFailed(workerId: String, workId: String)

  case class Ack(id: String)

  // Messages to Workers

  // type of clients
  val syncClient    = 1
  val asyncClient   = 2

  // type of workers
  val todoWorker    = "todo"
  val searchWorker  = "search"
  val dataWorker    = "data"

  // state machine protocol
  val initState     = 0
  val prepState     = 1
  val validateState = 2
  val searchState   = 3
  val finalState    = 10
  val errorState    = -1
}