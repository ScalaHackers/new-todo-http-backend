package io.github.scalahackers.service

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest._
import spray.json._


class TodoSpecs extends Suite
    with ScalatestRouteTest
    with RouteTest
    with WordSpecLike
    with ShouldMatchers
    with TodoRoutes
    with TodoManager {

  "The Todo backend" should {
//    "respond to a POST with the todo which was posted to it" in {
//      Post("/todostxs", HttpEntity(`application/json`, """{ "extid": "123456", "request": "a todo" }""")) ~> routes ~> check {
//        status should equal(StatusCodes.OK)
//        entityAs[JsObject].fields("request") should equal(JsString("a todo"))
//      }
//    }

    "create a todo with an response field" in {
      Post("/todostxs", HttpEntity(`application/json`, """{ "extid": 123456, "request": {"type": "search", "task": "a todo"}}""")) ~> routes ~> check {
        status should equal(StatusCodes.OK)
        entityAs[JsObject].fields("response") should equal(JsString("complete"))
      }
    }
  }
}
