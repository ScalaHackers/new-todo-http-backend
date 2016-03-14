package io.github.scalahackers.todo

import akka.actor._
import com.datainc.pipeline.workflow.TodoManagerActor.WorkerState
import com.datainc.pipeline.workflow.TodoManagerActor.WorkerState
import com.datainc.pipeline.workflow.{MasterWorkerProtocol, WorkState}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait TodoStorage {
  implicit val system: ActorSystem

  lazy val todoStorage: ActorRef = system.actorOf(Props(new TodoStorageActor))
}

object TodoStorageActor {
  sealed trait Command
  case object Get extends Command
  case class Get(id: String) extends Command
  case class Add(todo: TodoUpdate) extends Command
  case class Update(id: String, todo: TodoUpdate) extends Command
  case class Delete(id: String) extends Command
  // other case class
  case object Clear extends Command
}
class TodoStorageActor extends Actor with TodoTable {
  import TodoStorageActor._
  import driver.api._

  // workers state is not event sourced
  private var workers = Map[String, WorkerState]()

  // workState is event sourced
  private var workState = WorkState.empty

  def receive = {
    case JobProtocol.RegisterWorker(workerId) =>
      if (workers.contains(workerId)) {
        workers += (workerId -> workers(workerId).copy(ref = sender()))
      } else {
        log.info("Worker registered: {}", workerId)
        workers += (workerId -> WorkerState(sender(), status = Idle))
        if (workState.hasWork)
          sender() ! JobProtocol.WorkIsReady
      }

    case Get =>
      sender() ! Await.result(db.run(todos.result), Duration.Inf)
    case Get(id) =>
      sender() ! Await.result(db.run(todos.filter(_.id === id).result.head), Duration.Inf)
    case Add(todoUpdate) =>
      todoUpdate.title.map(Todo.create(_, todoUpdate)) match {
        case Some(todo) =>
          Await.result(db.run(todos += todo), Duration.Inf)
          sender() ! todo
          // hd: to find which worker is free
          workers.foreach {
            // send to many???
            case (_, WorkerState(ref, Idle)) => ref ! todo

            case _ => // busy
          }
          // hd
        case None =>
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
  }
}
