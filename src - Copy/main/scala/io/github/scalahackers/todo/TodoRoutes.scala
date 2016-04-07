package io.github.scalahackers.todo

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.pattern._
import akka.util._

import scala.concurrent.duration._

trait TodoRoutes extends TodoMarshalling
  with TodoManagerProvider {

  import ManagerProtocol._
  import JobProtocol._

  implicit val timeout: Timeout = 30 seconds

  def routes = {
    (respondWithHeaders(
      `Access-Control-Allow-Origin`.`*`,
      `Access-Control-Allow-Headers`("Accept", "Content-Type"),
      `Access-Control-Allow-Methods`(GET, HEAD, POST, DELETE, OPTIONS, PUT, PATCH)
    ) & extract(_.request.getUri())) { uri =>
      implicit val todoFormat = todoFormatFor(uri.path("/todostxs").toString)
      pathPrefix("todostxs") {
        pathEnd {
          get {
            onSuccess(todoManager ? ManagerProtocol.Get) { todos =>
              complete(StatusCodes.OK, todos.asInstanceOf[Iterable[TodoTxs]])
            }
          } ~
            post {
              entity(as[TodoUpdate]) { update =>
                onSuccess(todoManager ? ManagerProtocol.TodoRequest(update, todoState)) { todo =>
                  complete(StatusCodes.OK, todo.asInstanceOf[TodoTxs])
                }
              }
            } ~
            delete {
              onSuccess(todoManager ? ManagerProtocol.Clear) { _ =>
                complete(StatusCodes.OK)
              }
            }
        } ~ {
          path(Segment) { id =>
            get {
              onSuccess(todoManager ? ManagerProtocol.Get(id)) { todo =>
                complete(StatusCodes.OK, todo.asInstanceOf[TodoTxs])
              }
            } ~
              patch {
                entity(as[TodoUpdate]) { update =>
                  onSuccess(todoManager ? ManagerProtocol.Update(id, update)) { todo =>
                    complete(StatusCodes.OK, todo.asInstanceOf[TodoTxs])
                  }
                }
              } ~
              delete {
                onSuccess(todoManager ? ManagerProtocol.Delete(id)) { _ =>
                  complete(StatusCodes.OK)
                }
              }
          }
        }
      } ~
      path("searchtxs") {
        post {
          entity(as[TodoUpdate]) { update =>
            onSuccess(todoManager ? ManagerProtocol.TodoRequest(update, searchState)) { todo =>
              complete(StatusCodes.OK, todo.asInstanceOf[TodoTxs])
            }
          }
        }
      } ~
      path("remotework") {
        post {
          entity(as[TodoUpdate]) { update =>
            onSuccess(todoManager ? ManagerProtocol.TodoRequest(update, remoteState)) { todo =>
              complete(StatusCodes.OK, todo.asInstanceOf[TodoTxs])
            }
          }
        }
      } ~
      path("notifytxs") {
        get {
          complete(StatusCodes.OK)
        } ~
        post {
            entity(as[TodoUpdate]) { update =>
              onSuccess(todoManager ? ManagerProtocol.TxsNotify(update, doneSubState)) { todo =>
                complete(StatusCodes.OK, todo.asInstanceOf[TodoTxs])
              }
            }
          }
      } ~
      path("") {
        get {
          complete(StatusCodes.OK)
        }
      } ~
      options {
        complete(StatusCodes.OK)
      }
    }
  }
}
