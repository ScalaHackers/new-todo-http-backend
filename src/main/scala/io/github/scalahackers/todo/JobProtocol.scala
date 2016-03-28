package io.github.scalahackers.todo

object JobProtocol {

  // request/response message class
  sealed trait BaseRequest
  sealed trait BaseResponse

  // Messages from clients/external to manager
  case class RegisterClient(clientName: String, clientType: Int) extends BaseRequest
  case class NotifyClient(clientName: String, result: Any) extends BaseRequest
  case class ClientRequest(extid: String, request: BaseRequest) extends BaseRequest
  case class ClientResponse(extid: String, response: BaseResponse) extends BaseRequest


  // Messages from Workers to manager
  case class RegisterWorker(workerId: String, workerType: String)  extends BaseRequest
  case class UnRegisterWorker(workerId: String, workerType: String)  extends BaseRequest
  case class WorkerRequestsWork(workerId: String)  extends BaseRequest
  case object WorkIsReady  extends BaseRequest
  case class WorkIsDone(workerId: String, workId: String, result: Any)  extends BaseRequest
  case class WorkFailed(workerId: String, workId: String)  extends BaseRequest
  case class Ack(id: String)  extends BaseRequest

  // type of workers
  val todoWorkerType = "TODOWORKER"
  val searchWorkerType = "TODOWORKER"
  val dataWorkerType = "DATAWORKER"
  val enrollWorkerType = "ENROLLWORKER"

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