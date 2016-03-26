package io.github.scalahackers.todo

object JobProtocol {

  // Messages from clients/external to manager
  case class RegisterClient(clientName: String, clientType: Int)
  case class NotifyClient(clientName: String, result: Any)
  case class ClientRequest(extid: String, request: BaseRequest)
  case class ClientResponse(extid: String, response: BaseResponse)


  // Messages from Workers to manager
  case class RegisterWorker(workerId: String, workerType: String)
  case class UnRegisterWorker(workerId: String, workerType: String)
  case class WorkerRequestsWork(workerId: String)
  case object WorkIsReady
  case class WorkIsDone(workerId: String, workId: String, result: Any)
  case class WorkFailed(workerId: String, workId: String)
  case class Ack(id: String)

  // request/response message class
  case class BaseRequest(extid: String, group: String, url: String, option: String)
  case class BaseResponse(extid: String, result: String, url: String, option: String)

  // type of workers
  val todoWorker    = "todo"
  val searchWorker  = "search"
  val dataWorker    = "data"
  val enrollWorker  = "enroll"

  // state machine protocol： main states, change to string later
  val initState     = "INIT"
  val prepState     = "PREP"
  val validateState = "VALIDATE"
  val searchState   = "SEARCH"
  val enrollState   = "ENROLL"
  val finalState    = "FINAL"
  val errorState    = "ERROR"

  // state machine protocol： sub states , change to string later
  val newSubState   = "NEWSUB"
  val wipSubState   = "WIPSUB"
  val doneSubState  = "DONESUB"

  // state machine will be a config map file in application.conf, like BPM
}