package io.github.scalahackers.service

import java.util.UUID

import akka.actor._

// reference to master/TodoStorage
abstract class BaseWorker(managerRef: ActorRef)
  extends Actor with ActorLogging {
}