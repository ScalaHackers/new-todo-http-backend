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

  // type of workers
  val todoWorker    = "todo"
  val searchWorker  = "search"
  val dataWorker    = "data"

  // state machine protocol： main states
  val initState     = 0
  val prepState     = 1
  val validateState = 2
  val searchState   = 3
  val finalState    = 10
  val errorState    = -1

  // state machine protocol： sub states
  val newSubState   = 100
  val wipSubState   = 101
  val doneSubState  = 103

  // state machine will be a config map file, like BPM
}