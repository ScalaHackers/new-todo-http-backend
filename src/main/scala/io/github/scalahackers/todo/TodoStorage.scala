package io.github.scalahackers.todo

import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait TodoStorage {
  lazy val todoStorage: ActorRef = system.actorOf(Props(new TodoStorageActor))

  implicit val system: ActorSystem
}

object TodoStorageActor {

  // command
  sealed trait Command

  case class Get(id: String) extends Command

  case class Add(todo: TodoUpdate) extends Command

  case class Update(id: String, todo: TodoUpdate) extends Command

  case class Delete(id: String) extends Command

  case class Response(result: TodoUpdate) extends Command

  case object Get extends Command

  // other case class
  case object Clear extends Command

  // worker state table
  private sealed trait WorkerStatus

  private case class Busy(workId: String) extends WorkerStatus

  private case class WorkerState(ref: ActorRef, status: WorkerStatus)

  private case object Idle extends WorkerStatus

  // clients/front-end state table
  private case class ClientState(ref: ActorRef, clientId: String)

}

class TodoStorageActor extends Actor with TodoTable with ActorLogging {

  import TodoStorageActor._
  import driver.api._

  // workers state is not event sourced
  private var clients = Map[String, ClientState]()

  // workers state is not event sourced
  private var workers = Map[String, WorkerState]()

  // init children workers, we will need a set of workers
  val worker = context.actorOf(Props(new TodoWorker(self)))

  def isIdleWorker(state: WorkerState): Boolean = state match {
    case WorkerState(_, Idle) => true
    case _ => false
  }

  def receive = {
    case JobProtocol.RegisterWorker(workerId) =>
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
      todoUpdate.title.map(Todo.create(_, todoUpdate)) match {
        case Some(todo) =>
          Await.result(db.run(todos += todo), Duration.Inf)
          // sender() ! Ack
          // save the senders into table Clients
          // hd: to find which worker is free
          //workers.find((k, v) => isIdleWorker(v)).foreach(_._1 ! todo)
          println("I am here")
          workers.find {
            case (_, WorkerState(ref, Idle)) => true
          } foreach {
            case (_, WorkerState(ref, _)) => ref ! todo
          }
          //if no available worker, we will put this task on hold

        case none => // no match
          sender() ! Status.Failure(new IllegalArgumentException("Insufficient data"))
      }
    case Update(id, update) =>
      for (old <- Await.result(db.run(todos.filter(_.id === id).result.headOption), Duration.Inf)) {
        Await.result(db.run(todos.filter(_.id === id).update(Todo.create(old, update))), Duration.Inf)
      }
      self.forward(Get(id))
    case Delete(id) =>
      Await.result(db.run(todos.filter(_.id === id).delete), Duration.Inf)
      sender() ! Status.Success()
    case Clear =>
      Await.result(db.run(todos.delete), Duration.Inf)
      sender() ! Status.Success()

    case Response(todoUpdate) =>
      println("response is received from worker")
      todoUpdate.title.map(Todo.create(_, todoUpdate)) match {
        case Some(todo) =>
          Await.result(db.run(todos += todo), Duration.Inf)
        // hd: to find which front end is sender
        // find the frontend from table Clients
        //workers.find((k, v) => isIdleWorker(v)).foreach(_._1 ! todo)
        //sender() ! todoResult

        case None => // no match
          sender() ! Status.Failure(new IllegalArgumentException("Insufficient data"))
        // return to HTTP frontend
      }
  }
}
