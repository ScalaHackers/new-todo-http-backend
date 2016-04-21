package io.github.scalahackers.service

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
  case class WorkerAck(id: String)  extends BaseRequest

  // type of workers
//  val todoWorkerType = "TODOWORKER"
//  val searchWorkerType = "SEARCHWORKER"
//  val dataWorkerType = "DATAWORKER"
//  val enrollWorkerType = "ENROLLWORKER"

  // state machine protocol： main states, change to string later
  val initState     = "INIT"
  val prepState     = "PREP"
  val todoState     = "TODO"
  val searchState   = "SEARCH"
  val remoteState   = "REMOTE"
  val enrollState   = "ENROLL"
  val finalState    = "FINAL"
  val errorState    = "ERROR"

  // state machine protocol： sub states , change to string later
  val newSubState   = "NEWSUB"
  val wipSubState   = "WIPSUB"
  val doneSubState  = "DONESUB"

  // state machine will be a config map file in application.conf, like BPM
}

object ManagerProtocol {

  // state machine commands
  sealed trait StateRequest

  case class Get(id: String) extends StateRequest

  case class TodoRequest(todo: TodoUpdate, startState: String) extends StateRequest

  case class Update(id: String, todo: TodoUpdate) extends StateRequest

  case class Delete(id: String) extends StateRequest

  // response from workers
  case class WorkerResponse(workerId: String, state: String, result: TodoTxs, update: TodoUpdate)
    extends StateRequest

  // notify from pipeline
  case class TxsNotify(todo: TodoUpdate, subState: String) extends StateRequest

  case object Get extends StateRequest

  case object Clear extends StateRequest

  case class Ack(msg: String) extends StateRequest
}
