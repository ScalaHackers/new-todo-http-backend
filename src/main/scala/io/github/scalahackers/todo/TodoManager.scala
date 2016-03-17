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

  case class Response(result: TodoUpdate) extends Command

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
  // for
  val todoWorker = context.actorOf(Props(new TodoWorker(self)))
  //val searchWorker = context.actorOf(Props(new SearchWorker(self)))

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

    case Get =>
      sender() ! Await.result(db.run(todos.result), Duration.Inf)
    case Get(id) =>
      sender() ! Await.result(db.run(todos.filter(_.id === id).result.head), Duration.Inf)
    case Add(todoUpdate) =>
      todoUpdate.request.map(TodoTxs.create(_, todoUpdate)) match {
        case Some(todo) =>
          Await.result(db.run(todos += todo), Duration.Inf)
          // if sender() is not in clients map yet, add it.
          addTxsClientMap(todo.id, sender())
          // sender() ! Ack
          workers.find {
            case (_, WorkerState(ref, Idle)) => true
          } foreach {
            case (_, WorkerState(ref, _)) => ref ! todo
          }
          //if no available worker, we will queue this task in todos table

        case None => // no match, expcetion handling later
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

    case Response(todoUpdate) =>
      println("response is received from {} worker" + sender().toString())
      // move to next state, if final, return to client
      todoUpdate.request.map(TodoTxs.create(_, todoUpdate)) match {
        case Some(todo) =>
          Await.result(db.run(todos += todo), Duration.Inf)
          // hd: to find which front end is sender
          // find the frontend from table Clients
          println("look for client to send response for {}" + todo.id)
          clients.get(todo.id) match {
            case Some(ref) =>
              ref ! todo
              println("response is + " + todo.toString)
            case _ =>
          }

        case None => // no match, expcetion handling later
          sender() ! Status.Failure(new IllegalArgumentException("Insufficient data"))
        // return to HTTP frontend
      }
      // check if there is pending txs in queue of todos, schedule it if so.
  }
}
