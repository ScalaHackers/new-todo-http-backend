package io.github.scalahackers.todo

import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration.Duration



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
  case class Response(result: TodoTxs, update: TodoUpdate) extends Command

  case object Get extends Command

  case object Clear extends Command

  // worker state table
  private sealed trait WorkerStatus

  private case class Busy(workId: String) extends WorkerStatus

  private case object Idle extends WorkerStatus

  private case class WorkerState(ref: ActorRef, status: WorkerStatus)

}

class TodoManagerActor extends Actor with TodoTxsTable with ActorLogging {

  import TodoManagerActor._
  import driver.api._

  // workers state is not event sourced
  private var clients = Map[String, ActorRef]()

  // workers state is not event sourced
  private var workers = Map[String, WorkerState]()

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
    case JobProtocol.RegisterWorker(workerId, workerType) =>
      if (workers.contains(workerId)) {
        workers += (workerId -> workers(workerId).copy(ref = sender()))
      } else {
        log.info("Worker registered: {}", workerId)
        workers += (workerId -> WorkerState(sender(), status = Idle))
      }

    case JobProtocol.UnRegisterWorker(workerId, workerType) =>
      if (workers.contains(workerId)) {
        workers -= workerId
      } else {
        log.info("There is no such worker registered: {}", workerId)
      }

    case Get =>
      sender() ! Await.result(db.run(todos.result), Duration.Inf)

    case Get(id) =>
      sender() ! Await.result(db.run(todos.filter(_.id === id).result.head), Duration.Inf)

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
          workers.find {
            case (_, WorkerState(ref, Idle)) => true
          } foreach {
            case (_, WorkerState(ref, _)) => ref ! todo
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

    case Response(todo, update) =>
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
      // check if there is pending txs in queue of todos, schedule it if so.
      Schedule(sender(), JobProtocol.validateState)
  }

  def Schedule(freeWorker: ActorRef, pendingState: String): Unit = {
    log.info("check if any pending txs for this type of workers.")
    for (pending <- Await.result(db.run(todos.filter(_.state =!= pendingState).result.headOption), Duration.Inf)) {
      freeWorker ! pending
    }
  }
}
