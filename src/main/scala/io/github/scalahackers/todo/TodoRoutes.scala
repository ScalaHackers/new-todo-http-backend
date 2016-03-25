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

  implicit val timeout: Timeout = 10 seconds

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
            onSuccess(todoManager ? TodoManagerActor.Get) { todos =>
              complete(StatusCodes.OK, todos.asInstanceOf[Iterable[TodoTxs]])
            }
          } ~
            post {
              entity(as[TodoUpdate]) { update =>
                onSuccess(todoManager ? TodoManagerActor.Add(update)) { todo =>
                  complete(StatusCodes.OK, todo.asInstanceOf[TodoTxs])
                }
              }
            } ~
            delete {
              onSuccess(todoManager ? TodoManagerActor.Clear) { _ =>
                complete(StatusCodes.OK)
              }
            }
        } ~ {
          path(Segment) { id =>
            get {
              onSuccess(todoManager ? TodoManagerActor.Get(id)) { todo =>
                complete(StatusCodes.OK, todo.asInstanceOf[TodoTxs])
              }
            } ~
              patch {
                entity(as[TodoUpdate]) { update =>
                  onSuccess(todoManager ? TodoManagerActor.Update(id, update)) { todo =>
                    complete(StatusCodes.OK, todo.asInstanceOf[TodoTxs])
                  }
                }
              } ~
              delete {
                onSuccess(todoManager ? TodoManagerActor.Delete(id)) { _ =>
                  complete(StatusCodes.OK)
                }
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
              onSuccess(todoManager ? TodoManagerActor.Add(update)) { todo =>
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
