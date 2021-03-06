package io.github.scalahackers.service

import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import scala.collection.mutable.Map
import scala.collection.mutable.ArrayBuffer

import com.typesafe.config.ConfigFactory

import akka.actor.{ Props, Deploy, Address, AddressFromURIString }
//import akka.remote.RemoteScope


trait TodoManager {
  lazy val todoManager: ActorRef = system.actorOf(Props(new TodoManagerActor))

  implicit val system: ActorSystem
}

object TodoManagerActor {

  // worker state table
  sealed trait WorkerStatus

  case class Busy(workId: String) extends WorkerStatus

  case object Idle extends WorkerStatus

  case class WorkerState(ref: ActorRef, status: WorkerStatus)
}

//class TodoManagerActor extends Actor with TodoTxsTable with ActorLogging {
class TodoManagerActor extends BaseManager {

  import TodoManagerActor._
  import driver.api._
  import JobProtocol._
  import ManagerProtocol._

  // workers state is not event sourced
  private var clients = Map[String, ActorRef]()

  private var todoWorkers     = Map[String, WorkerState]()
  private var searchWorkers   = Map[String, WorkerState]()
  private var noneWorkers     = Map[String, WorkerState]()
  private var remoteWorkers   = Map[String, WorkerState]()
  //more

  private var stateWorkerMap = Map[String, Map[String, WorkerState]]()

  private var defaultStateMachineList = ArrayBuffer[String]()

  override def preStart(): Unit = {
    // workers' map by (workerType, set of workers for state)
    stateWorkerMap = Map(initState -> noneWorkers,
      todoState -> todoWorkers,
      searchState -> searchWorkers,
      remoteState -> remoteWorkers,
      finalState -> noneWorkers)

    // map of stateMachine to workerType
    defaultStateMachineList = ArrayBuffer(initState,
      todoState,
      searchState,
      //remoteState,
      finalState)
    log.info("TBD: to load configed workflow!")

    // init children todoWorkers, we will need a set of workers
    var numTodoWorkers = 1
    for (i <- 0 until numTodoWorkers ) {
      context.actorOf(Props(new TodoWorker(self)))
      context.actorOf(Props(new SearchWorker(self)))
      //context.actorOf(Props(new NoneWorker(self)))
      startRemoteWorker(5000, "remoteWorker")
    }
  }

  def startRemoteWorker(port: Int, actorName: String): Unit = {
    // load worker.conf
    val workerConf = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
      withFallback(ConfigFactory.load("worker"))
    val system = ActorSystem("RemoteWorkerSystem", workerConf)

    val remoteWorker = system.actorOf(Props(new RemoteActor(self)), actorName)
    remoteWorker ! ManagerProtocol.Ack("started!")
  }

  def receive = {
    case RegisterWorker(workerId, workerType) => {
      //      var workers = stateWorkerMap.get(workerType)
      //      workers.foreach( w =>
      //        w += (workerId -> WorkerState(sender (), Idle) )
      //      )
      //    }
      stateWorkerMap.get(workerType) match {
        case Some(sameTypeWorkers) => sameTypeWorkers.get(workerId) match {
          case Some(worker) =>
            log.info("Worker re-registered: {} {}", workerId, workerType)
            sameTypeWorkers += (workerId ->WorkerState(sender(), Idle))
          case None =>
            log.info("New worker registered: {} {}", workerId, workerType)
            sameTypeWorkers += (workerId ->WorkerState(sender(), Idle))
        }

        case None =>
          log.error("No such type of worker is supported: {}", workerType)
      }
    }

    case UnRegisterWorker(workerId, workerType) => {
      stateWorkerMap.get(workerType) match {
        case Some(sameTypeWorkers) => sameTypeWorkers.get(workerId) match {
          case Some(worker) =>
            sameTypeWorkers -= workerId
          case None =>
            log.info("There is no such worker registered: {}", workerId)
        }

        case None =>
          log.error("No such type of worker is supported: {}", workerType)
      }
    }

    case Get =>
      sender() ! Await.result(db.run(todos.result), Duration.Inf)
    case Get(id) =>
      sender() ! Await.result(db.run(todos.filter(_.id === id).result.head), Duration.Inf)
    case TodoRequest(todoUpdate, startState) =>
      log.info("startState: %s testid: %s is in manager actor: %s".format(startState, todoUpdate.extid, self.toString()))
      todoUpdate.request.map(TodoTxs.create(_, todoUpdate)) match {
        case Some(todo) =>
          println("start at state {}", todo.state)
          Await.result(db.run(todos += todo), Duration.Inf)
          // if sender() is not in clients map yet, add it.
          log.info("new txsid: %s is created in manager actor: %s".format(todo.id, self.toString()))
          addTxsClientMap(todo.id, sender())
          // sender() ! Ack
          var workersMap = stateWorkerMap.get(startState)
          workersMap match {
            case Some(workers) =>
              //workers.find {case (_, WorkerState(ref, Idle)) => true
              workers.find(entry => entry._2.status == Idle).foreach {
                case (myWorkerId, WorkerState(ref, _)) =>
                  ref ! todo
                  changeWorkerStatus(myWorkerId, startState, todo.id, Busy(todo.id))
                //otherwise, there is no available worker, we will queue this task in txs table
              }
            case None =>
              log.info("No available worker for new txsid: {}, save in queue", todo.id)
          }

        case None => // no match, exception handling later
          sender() ! Status.Failure(new IllegalArgumentException("Insufficient data"))
      }

    case Update(id, update) =>
      for (old <- Await.result(db.run(todos.filter(_.id === id).result.headOption), Duration.Inf)) {
        Await.result(db.run(todos.filter(_.id === id).update(TodoTxs.create(old, update))), Duration.Inf)
      }
      self.forward(Get(id))
    case Delete(id) =>
      Await.result(db.run(todos.filter(_.id === id).delete), Duration.Inf)
      sender() ! Status.Success()
    case Clear =>
      Await.result(db.run(todos.delete), Duration.Inf)
      sender() ! Status.Success()

    // message from Pipeline via RESTful interface
    case TxsNotify(todoUpdate, subState) =>
      log.info("notify for accessionid: %s is in manager actor: %s".format(todoUpdate.extid, self.toString()))

      //      for (old <- Await.result(db.run(todos.filter(_.id === id).result.headOption), Duration.Inf)) {
      //        Await.result(db.run(todos.filter(_.id === id).update(TodoTxs.create(old, update))), Duration.Inf)
      //      }
      //      self.forward(Get(id))
      val update = new TodoUpdate(todoUpdate.extid,
        todoUpdate.request,
        Option(JobProtocol.finalState),
        Option(subState),
        Option("complete"), // response
        None, None, None )

      //for (old <- Await.result(db.run(todos.filter(_.extid === update.extid).result.headOption), Duration.Inf)) {
      //   Await.result(db.run(todos.filter(_.extid === update.extid).update(TodoTxs.create(old, update))), Duration.Inf)
      //}
      //Query(todos).where(extid = update.extid ).map{txs => txs.state ~ txs.substate ~ txs.response}.update
      //        ((StateProtocol.finalState, StateProtocol.doneSubState, todoUpdate.response))
      //db.run(todos.filter(_.extid === update.extid).map(txs => (txs.state, txs.substate, txs.response))).
      //      update((StateProtocol.finalState, StateProtocol.doneSubState, todoUpdate.response))
      val columns = for {txs <- todos if txs.extid === update.extid } yield (txs.state, txs.substate, txs.response)
      Await.result(db.run(columns.update((JobProtocol.finalState, subState, todoUpdate.response.getOrElse("no response")))),
        Duration.Inf)

      log.info("notify for accessionid: %s is received".format(update.extid))
      sender() ! TodoTxs.create(update.request.get, update)
    //      sender() ! ClientAck(todoUpdate.extid.getOrElse("error extid"), "",
    //          ResponseData(todoUpdate.extid.getOrElse("error extid"), "completed!"))
    //      self.forward(Get(id))

      // move txs to next state
    // message from Workers
    case WorkerResponse(retWorkerId, state, todo, update) =>
      log.info("response for accessionid: %s is received in manager actor: %s, from worker: %s".format(todo.id, self.toString(),
        sender().toString()))
      for (old <- Await.result(db.run(todos.filter(_.id === todo.id).result.headOption), Duration.Inf)) {
        Await.result(db.run(todos.filter(_.id === todo.id).update(TodoTxs.create(old, update))), Duration.Inf)
        // hd: to find which front end is sender
        log.debug("txs: %s is ready to send back".format(todo.id))
        // find the frontend from table Clients
        clients.get(todo.id).foreach(ref => {
          ref ! todo
          log.info("txs: %s has been sent back to client : %s".format(todo.id, ref.toString()))
          log.info("The response is + " + todo.toString)
        })
      }
      // newly added
      log.debug("changeWorkerStatus: %s %s %s %s".format(retWorkerId, state, todo.id, Idle.toString()))
      changeWorkerStatus(retWorkerId, state, todo.id, Idle)

      // move to next state
      if (!(state.equalsIgnoreCase(defaultStateMachineList.last))) {
        changeTxsState(todo, getNextState(state), newSubState, sender(), retWorkerId, state)
        log.debug("txs %s move to next state %s ".format(todo.id, getNextState(state)))
      }
  }

  def getNextState(state: String): String = {
    val curStateIdx = defaultStateMachineList.indexOf(state)
    val nextIdx =
      if (curStateIdx == defaultStateMachineList.length - 1) defaultStateMachineList.length - 1
      else curStateIdx + 1

    return defaultStateMachineList(nextIdx)
  }

  def reSchedule: Unit = {
    // check all pending txs and send to available workers
    for (pending <- Await.result(db.run(todos.filter(_.substate === newSubState).
      filter(_.state =!= finalState).result), Duration.Inf)) {
        var workersMap = stateWorkerMap.get(pending.state)
        workersMap match {
          case Some(workers) =>
            workers.find(entry => entry._2.status == Idle).foreach {
              case (myWorkerId, WorkerState(ref, _)) =>
                ref ! pending
                changeWorkerStatus(myWorkerId, pending.state, pending.id, Busy(pending.id))
            }
          case None =>
            log.info("No available worker for new txsid: {}, save in queue", pending.id)
        }
      }
  }

  def changeWorkerStatus(workerId: String, workerType: String, txsId: String, newStatus: WorkerStatus): Unit = {
    stateWorkerMap.get(workerType) match {
      case Some(sameTypeWorkers) => sameTypeWorkers.get(workerId) match {
        case Some(worker) =>
          //sameTypeWorkers += (workerId -> WorkerState(sender(), newStatus))
          sameTypeWorkers += (workerId -> worker.copy(status = newStatus))
        case _ ⇒
      }
      case _ =>
    }
  }

  def changeTxsState(todo: TodoTxs, newState: String, newSubState: String,
                     freeWorker: ActorRef, freeWorkerId: String, pendingState: String): Unit = {
    val update = new TodoUpdate(Option(todo.extid),
      Option(todo.request),
      Option(newState),
      Option(newSubState),
      None,
      None, None, None )

    for (old <- Await.result(db.run(todos.filter(_.id === todo.id).result.headOption), Duration.Inf)) {
      Await.result(db.run(todos.filter(_.id === todo.id).update(TodoTxs.create(old, update))), Duration.Inf)
    }

    // TBD: check if there is pending txs in queue of todos, schedule it if so.
    reSchedule
  }

  def addTxsClientMap(id: String, sender: ActorRef) = {
    clients += (id -> sender)
  }
}
