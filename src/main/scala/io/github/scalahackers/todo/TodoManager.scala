package io.github.scalahackers.todo

import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import scala.collection.mutable.Map


trait TodoManager {
  lazy val todoManager: ActorRef = system.actorOf(Props(new TodoManagerActor))

  implicit val system: ActorSystem
}

object TodoManagerActor {

  // commands
  sealed trait Command

  case class Get(id: String) extends Command

  case class Add(todo: TodoUpdate) extends Command

  case class Update(id: String, todo: TodoUpdate) extends Command

  case class Delete(id: String) extends Command

  //case class Response(result: TodoUpdate) extends Command
  case class Response(workerId: String, state: String, result: TodoTxs, update: TodoUpdate) extends Command

  case object Get extends Command

  case object Clear extends Command

  // worker state table
  private sealed trait WorkerStatus

  private case class Busy(txsId: String) extends WorkerStatus

  private case object Idle extends WorkerStatus

  private case class WorkerState(ref: ActorRef, status: WorkerStatus)

}

class TodoManagerActor extends Actor with TodoTxsTable with ActorLogging {

  import TodoManagerActor._
  import JobProtocol._
  import driver.api._

  // workers state is not event sourced
  private var clients = Map[String, ActorRef]()

  private var todoWorkers = Map[String, WorkerState]()
  private var searchWorkers = Map[String, WorkerState]()

  // workers' map by (workerType, set of workers for state)
  private var stateWorkerMap = Map(todoWorkerType -> todoWorkers,
                  searchWorkerType -> searchWorkers)

  // init children todoWorkers, we will need a set of workers
  var numTodoWorkers = 2
  for (i <- 1 until numTodoWorkers ) {
    context.actorOf(Props(new TodoWorker(self)))
    context.actorOf(Props(new SearchWorker(self)))
  }

  def addTxsClientMap(id: String, sender: ActorRef) = {
    clients += (id -> sender)
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
            log.info("Worker re-registered: {}", workerId)
            sameTypeWorkers += (workerId ->WorkerState(sender(), Idle))
          case None =>
            log.info("New worker registered: {}", workerId)
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

    // for TodoWorker
    case Add(todoUpdate) =>

      // data validation
      log.info("extid: %s is in manager actor: %s".format(todoUpdate.extid, self.toString()))
      todoUpdate.request.map(TodoTxs.create(_, todoUpdate)) match {
        case Some(todo) =>
          Await.result(db.run(todos += todo), Duration.Inf)
          // if sender() is not in clients map yet, add it.
          log.info("new txsid: %s is created in manager actor: %s".format(todo.id, self.toString()))
          addTxsClientMap(todo.id, sender())
          // sender() ! Ack
          var workersMap = stateWorkerMap.get(todoWorkerType)
          workersMap match {
            case Some(workers) =>
              workers.find {
                case (_, WorkerState(ref, Idle)) => true
              } foreach {
                case (myWorkerId, WorkerState(ref, _)) =>
                  ref ! todo
                  // newly added
                  changeWorkerStatus(myWorkerId, todoWorkerType, todo.id, Busy(todo.id))
              }
            case None =>
              log.info("No available worker for new txsid: {}, save in queue", todo.id)
          }
          //otherwise, there is no available worker, we will queue this task in txs table

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

    case Response(retWorkerIt, state, todo, update) =>
      log.info("response for txsid: %s is received in manager actor: %s, from worker: %s".format(todo.id, self.toString(),
        sender().toString()))
      for (old <- Await.result(db.run(todos.filter(_.id === todo.id).result.headOption), Duration.Inf)) {
        Await.result(db.run(todos.filter(_.id === todo.id).update(TodoTxs.create(old, update))), Duration.Inf)
        // hd: to find which front end is sender
        // find the frontend from table Clients
        clients.get(todo.id) match {
          case Some(ref) =>
            ref ! todo
            log.info("txs: %s has been sent back to client : %s".format(todo.id, ref.toString()))
          case _ =>
        }
      }
      // newly added
      changeWorkerStatus(retWorkerIt, todoWorkerType, todo.id, Busy(todo.id))

      // move to next state
      //changeState(todo, state)
    
      // check if there is pending txs in queue of todos, schedule it if so.
      //schedule(sender(), validateState)
  }

  def schedule(freeWorker: ActorRef, pendingState: String): Unit = {
    log.info("check if any pending txs for this type of workers.")
    for (pending <- Await.result(db.run(todos.filter(_.state =!= pendingState).result.headOption), Duration.Inf)) {
      freeWorker ! pending
    }
  }

  def changeWorkerStatus(workerId: String, workerType: String, txsId: String, newStatus: WorkerStatus): Unit = {
    stateWorkerMap.get(workerType) match {
      case Some(sameTypeWorkers) => sameTypeWorkers.get(workerId) match {
        case Some(worker) =>
          sameTypeWorkers += (workerId -> WorkerState(sender(), newStatus))
        //sameTypeWorkers += (workerId -> worker.copy(status = newStatus))
        case _ ⇒
      }
      case _ =>
    }
  }

}
