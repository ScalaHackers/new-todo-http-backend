package io.github.scalahackers.todo

import akka.actor._

trait TodoStorageProvider {
  val todoStorage: ActorRef
}
